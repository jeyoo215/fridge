package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.ComboRecommendation;
import lombok.Getter;

@Getter
public class ComboRecommendResponse {
    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final Double comboScore;

    public ComboRecommendResponse(ComboRecommendation entity) {
        this.recipeId = entity.getRecipe().getRecipeId();
        this.recipeName = entity.getRecipe().getRecipeName();
        this.imageUrl = entity.getRecipe().getImageUrl();
        this.cookingTimeMinutes = entity.getRecipe().getCookingTimeMinutes();
        this.difficulty = entity.getRecipe().getDifficulty();
        this.comboScore = entity.getComboScore();
    }
}