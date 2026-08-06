package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.RecognizedIngredientResponse;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ImageRecognitionService {

    private static final String API_PROVIDER = "GoogleVision";
    private static final int MAX_RESULTS_PER_FEATURE = 10;
    private static final float MIN_SCORE = 0.6f;

    private final ImageAnnotatorClient imageAnnotatorClient;
    private final VisionLabelTranslator labelTranslator;
    private final IngredientRepository ingredientRepository;
    private final ImageRecognitionLogRepository imageRecognitionLogRepository;

    @Transactional
    public List<RecognizedIngredientResponse> recognize(Long userId, MultipartFile image) {
        // 인식 시도 자체는 항상 로그로 남김 (성공/실패 분석용)
        imageRecognitionLogRepository.save(
                ImageRecognitionLog.builder()
                        .userId(userId)
                        .imageFileName(image.getOriginalFilename())
                        .apiProvider(API_PROVIDER)
                        .build()
        );

        List<DetectedLabel> detectedLabels = detectLabels(image);

        // 같은 재료가 여러 라벨(예: "onion", "vegetable")로 중복 매칭될 수 있어 ingredientId 기준으로 가장 높은 confidence만 남김
        Map<Long, RecognizedIngredientResponse> bestMatches = new LinkedHashMap<>();
        for (DetectedLabel label : detectedLabels) {
            if (label.score() < MIN_SCORE) {
                continue;
            }
            String koreanKeyword = labelTranslator.toKoreanKeyword(label.text());
            if (koreanKeyword == null) {
                continue;
            }
            for (Ingredient ingredient : ingredientRepository.findTop10ByIngredientNameContaining(koreanKeyword)) {
                RecognizedIngredientResponse existing = bestMatches.get(ingredient.getIngredientId());
                if (existing == null || existing.getConfidenceScore() < label.score()) {
                    bestMatches.put(ingredient.getIngredientId(), new RecognizedIngredientResponse(
                            ingredient.getIngredientId(), ingredient.getIngredientName(), label.score()));
                }
            }
        }

        List<RecognizedIngredientResponse> results = new ArrayList<>(bestMatches.values());
        results.sort((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return results;
    }

    // Vision의 라벨/객체 인식 결과를 텍스트+신뢰도 형태로 통일해서 다루기 위한 값 객체.
    // (EntityAnnotation은 getDescription(), LocalizedObjectAnnotation은 getName()으로 이름 API가 달라서 통일이 필요함)
    private record DetectedLabel(String text, float score) {
    }

    private List<DetectedLabel> detectLabels(MultipartFile imageFile) {
        Image image;
        try {
            image = Image.newBuilder()
                    .setContent(ByteString.copyFrom(imageFile.getBytes()))
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽을 수 없습니다.", e);
        }

        // LABEL_DETECTION만으로는 사진 전체를 보고 "Food", "Produce" 같은 전역적인 라벨을 주로 반환해서
        // 한 사진에 재료가 여러 개 있어도 개별로 잘 안 잡힘. OBJECT_LOCALIZATION을 같이 써서
        // 사진 속 물체 하나하나를 개별 후보로 인식하도록 보완한다.
        Feature labelDetection = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(MAX_RESULTS_PER_FEATURE)
                .build();
        Feature objectLocalization = Feature.newBuilder()
                .setType(Feature.Type.OBJECT_LOCALIZATION)
                .setMaxResults(MAX_RESULTS_PER_FEATURE)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(labelDetection)
                .addFeatures(objectLocalization)
                .setImage(image)
                .build();

        BatchAnnotateImagesResponse batchResponse =
                imageAnnotatorClient.batchAnnotateImages(List.of(request));
        AnnotateImageResponse response = batchResponse.getResponses(0);

        if (response.hasError()) {
            throw new IllegalStateException("Vision API 인식 실패: " + response.getError().getMessage());
        }

        return Stream.concat(
                response.getLabelAnnotationsList().stream()
                        .map(label -> new DetectedLabel(label.getDescription(), label.getScore())),
                response.getLocalizedObjectAnnotationsList().stream()
                        .map(object -> new DetectedLabel(object.getName(), object.getScore()))
        ).toList();
    }
}
