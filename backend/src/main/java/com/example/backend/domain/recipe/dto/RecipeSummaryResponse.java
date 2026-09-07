package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.Recipe;
import lombok.Getter;

// 목록/검색 카드용 요약 DTO (상세 재료·조리순서는 안 담음, 상세 조회는 RecipeDetailResponse가 담당)
@Getter
public class RecipeSummaryResponse {
    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final String categoryName;

    public RecipeSummaryResponse(Recipe entity) {
        this.recipeId = entity.getRecipeId();
        this.recipeName = entity.getRecipeName();
        this.imageUrl = entity.getImageUrl();
        this.cookingTimeMinutes = entity.getCookingTimeMinutes();
        this.difficulty = entity.getDifficulty();
        this.categoryName = entity.getCategory() != null ? entity.getCategory().getCategoryName() : "미분류";  
    }
}