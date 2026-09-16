package com.example.backend.domain.social.dto;

import com.example.backend.domain.social.RecipeScrap;
import lombok.Getter;

// 마이페이지 "스크랩한 레시피" 목록에 필요한 만큼만 담은 응답
@Getter
public class ScrapedRecipeResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;

    public ScrapedRecipeResponse(RecipeScrap scrap) {
        this.recipeId = scrap.getRecipe().getRecipeId();
        this.recipeName = scrap.getRecipe().getRecipeName();
        this.imageUrl = scrap.getRecipe().getImageUrl();
        this.cookingTimeMinutes = scrap.getRecipe().getCookingTimeMinutes();
        this.difficulty = scrap.getRecipe().getDifficulty();
    }
}
