package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.ComboRecommendation;
import lombok.Getter;

@Getter
public class ComboRecommendResponse {
    private final Long recipeId;
    private final String recipeName;
    private final Double comboScore;

    public ComboRecommendResponse(ComboRecommendation entity) {
        this.recipeId = entity.getRecipe().getRecipeId();
        this.recipeName = entity.getRecipe().getRecipeName();
        this.comboScore = entity.getComboScore();
    }
}   