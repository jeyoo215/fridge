package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.Recipe;
import lombok.Getter;

@Getter
public class RecipeRecommendResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final long matchCount;
    private final int totalIngredientCount;
    private final int expiryPriorityScore; // 유통기한 임박 재료 활용도 점수 (FR-21)
    private final boolean hasAllTools; // 필요 조리도구를 전부 보유하고 있는지 (FR-22)
    private final boolean isUserCreated; // 커뮤니티 게시글이 승격되어 생성된 레시피인지

    public RecipeRecommendResponse(Recipe entity, long matchCount, int expiryPriorityScore, boolean hasAllTools) {
        this.recipeId = entity.getRecipeId();
        this.recipeName = entity.getRecipeName();
        this.imageUrl = entity.getImageUrl();
        this.cookingTimeMinutes = entity.getCookingTimeMinutes();
        this.difficulty = entity.getDifficulty();
        this.matchCount = matchCount;
        this.totalIngredientCount = entity.getRecipeIngredients().size();
        this.expiryPriorityScore = expiryPriorityScore;
        this.hasAllTools = hasAllTools;
        this.isUserCreated = "커뮤니티".equals(entity.getSource());
    }
}