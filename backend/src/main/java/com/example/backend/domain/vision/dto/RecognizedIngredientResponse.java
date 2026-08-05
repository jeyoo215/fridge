package com.example.backend.domain.vision.dto;

import lombok.Getter;

@Getter
public class RecognizedIngredientResponse {

    private final String label;
    private final float confidence;
    private final String category;

    public RecognizedIngredientResponse(String label, float confidence, String category) {
        this.label = label;
        this.confidence = confidence;
        this.category = category;
    }
}
