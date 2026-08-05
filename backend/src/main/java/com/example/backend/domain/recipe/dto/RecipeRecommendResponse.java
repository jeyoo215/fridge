package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.Recipe;
import lombok.Getter;

// 추천 목록 화면에 내려줄 응답 DTO (레시피 기본 정보 + 매칭 정보)
@Getter
public class RecipeRecommendResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final long matchCount;       // 보유 재료 중 이 레시피와 겹치는 재료 수
    private final int totalIngredientCount; // 레시피가 필요로 하는 전체 재료 수

    public RecipeRecommendResponse(Recipe entity, long matchCount) {
        this.recipeId = entity.getRecipeId();
        this.recipeName = entity.getRecipeName();
        this.imageUrl = entity.getImageUrl();
        this.cookingTimeMinutes = entity.getCookingTimeMinutes();
        this.difficulty = entity.getDifficulty();
        this.matchCount = matchCount;
        this.totalIngredientCount = entity.getRecipeIngredients().size();
    }
}