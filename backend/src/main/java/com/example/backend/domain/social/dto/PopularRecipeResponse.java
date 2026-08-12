package com.example.backend.domain.social.dto;

import com.example.backend.domain.recipe.Recipe;
import lombok.Getter;

@Getter
public class PopularRecipeResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final long likeCount;
    private final long reviewCount;

    public PopularRecipeResponse(Recipe recipe, long likeCount, long reviewCount) {
        this.recipeId = recipe.getRecipeId();
        this.recipeName = recipe.getRecipeName();
        this.imageUrl = recipe.getImageUrl();
        this.cookingTimeMinutes = recipe.getCookingTimeMinutes();
        this.difficulty = recipe.getDifficulty();
        this.likeCount = likeCount;
        this.reviewCount = reviewCount;
    }
}
