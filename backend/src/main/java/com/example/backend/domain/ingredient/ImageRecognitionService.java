package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.RecognizedIngredientResponse;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
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

@Service
@RequiredArgsConstructor
public class ImageRecognitionService {

    private static final String API_PROVIDER = "GoogleVision";
    private static final int MAX_LABELS = 10;
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

        List<EntityAnnotation> labels = detectLabels(image);

        // 같은 재료가 여러 라벨(예: "onion", "vegetable")로 중복 매칭될 수 있어 ingredientId 기준으로 가장 높은 confidence만 남김
        Map<Long, RecognizedIngredientResponse> bestMatches = new LinkedHashMap<>();
        for (EntityAnnotation label : labels) {
            if (label.getScore() < MIN_SCORE) {
                continue;
            }
            String koreanKeyword = labelTranslator.toKoreanKeyword(label.getDescription());
            if (koreanKeyword == null) {
                continue;
            }
            for (Ingredient ingredient : ingredientRepository.findTop10ByIngredientNameContaining(koreanKeyword)) {
                RecognizedIngredientResponse existing = bestMatches.get(ingredient.getIngredientId());
                if (existing == null || existing.getConfidenceScore() < label.getScore()) {
                    bestMatches.put(ingredient.getIngredientId(), new RecognizedIngredientResponse(
                            ingredient.getIngredientId(), ingredient.getIngredientName(), label.getScore()));
                }
            }
        }

        List<RecognizedIngredientResponse> results = new ArrayList<>(bestMatches.values());
        results.sort((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return results;
    }

    private List<EntityAnnotation> detectLabels(MultipartFile imageFile) {
        Image image;
        try {
            image = Image.newBuilder()
                    .setContent(ByteString.copyFrom(imageFile.getBytes()))
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽을 수 없습니다.", e);
        }

        Feature labelDetection = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(MAX_LABELS)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(labelDetection)
                .setImage(image)
                .build();

        BatchAnnotateImagesResponse batchResponse =
                imageAnnotatorClient.batchAnnotateImages(List.of(request));
        AnnotateImageResponse response = batchResponse.getResponses(0);

        if (response.hasError()) {
            throw new IllegalStateException("Vision API 인식 실패: " + response.getError().getMessage());
        }

        return response.getLabelAnnotationsList();
    }
}
