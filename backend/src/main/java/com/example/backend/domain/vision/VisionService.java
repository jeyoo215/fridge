package com.example.backend.domain.vision;

import com.example.backend.domain.vision.dto.RecognizedIngredientResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VisionService {

    private static final int MAX_LABELS = 10;
    private static final float MIN_SCORE = 0.6f;

    private final ImageAnnotatorClient imageAnnotatorClient;
    private final IngredientCategoryMapper categoryMapper;

    public List<RecognizedIngredientResponse> recognizeIngredients(MultipartFile imageFile) throws IOException {
        Image image = Image.newBuilder()
                .setContent(ByteString.copyFrom(imageFile.getBytes()))
                .build();

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

        List<RecognizedIngredientResponse> results = new ArrayList<>();
        Set<String> seenLabels = new HashSet<>();

        for (EntityAnnotation label : response.getLabelAnnotationsList()) {
            if (label.getScore() < MIN_SCORE) {
                continue;
            }
            String description = label.getDescription();
            if (!seenLabels.add(description.toLowerCase())) {
                continue;
            }
            String category = categoryMapper.mapToCategory(description);
            if (category == null) {
                continue;
            }
            results.add(new RecognizedIngredientResponse(description, label.getScore(), category));
        }

        return results;
    }
}
