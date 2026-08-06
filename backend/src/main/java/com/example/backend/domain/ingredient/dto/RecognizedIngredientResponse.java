package com.example.backend.domain.ingredient.dto;

import lombok.Getter;

@Getter
public class RecognizedIngredientResponse {

    private final Long ingredientId;
    private final String ingredientName;
    private final double confidenceScore; // 0.0 ~ 1.0

    public RecognizedIngredientResponse(Long ingredientId, String ingredientName, double confidenceScore) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.confidenceScore = confidenceScore;
    }
}
