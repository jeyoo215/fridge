package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeIngredient;
import com.example.backend.domain.recipe.CookingStep;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 레시피 상세조회 응답 DTO (FR-24, FR-22 조리도구/조미료)
@Getter
public class RecipeDetailResponse {

    private final Long recipeId;
    private final String categoryName;
    private final String recipeName;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    private final List<IngredientDetail> ingredients;
    private final List<StepDetail> steps;
    private final List<Long> toolIds;

    public RecipeDetailResponse(Recipe recipe) {
        this.recipeId = recipe.getRecipeId();
        this.categoryName = recipe.getCategory().getCategoryName();
        this.recipeName = recipe.getRecipeName();
        this.cookingTimeMinutes = recipe.getCookingTimeMinutes();
        this.difficulty = recipe.getDifficulty();
        this.imageUrl = recipe.getImageUrl();
        this.createdAt = recipe.getCreatedAt();
        this.ingredients = recipe.getRecipeIngredients().stream()
                .map(IngredientDetail::new)
                .toList();
        this.steps = recipe.getCookingSteps().stream()
                .sorted((a, b) -> Integer.compare(a.getStepOrder(), b.getStepOrder()))
                .map(StepDetail::new)
                .toList();
        this.toolIds = recipe.getRecipeTools().stream()
                .map(com.example.backend.domain.recipe.RecipeTool::getToolId)
                .toList();
    }

    // 재료 상세 (재료명 + 수량 + 단위 + 조미료 여부)
    @Getter
    public static class IngredientDetail {
        private final Long ingredientId;
        private final String ingredientName;
        private final BigDecimal quantity;
        private final String unit;
        private final boolean isSeasoning;

        public IngredientDetail(RecipeIngredient recipeIngredient) {
            this.ingredientId = recipeIngredient.getIngredient().getIngredientId();
            this.ingredientName = recipeIngredient.getIngredient().getIngredientName();
            this.quantity = recipeIngredient.getQuantity();
            this.unit = recipeIngredient.getUnit();
            this.isSeasoning = recipeIngredient.getIngredient().isSeasoning();
        }
    }

    // 조리 단계 상세 (순서 + 설명)
    @Getter
    public static class StepDetail {
        private final Integer stepOrder;
        private final String description;

        public StepDetail(CookingStep cookingStep) {
            this.stepOrder = cookingStep.getStepOrder();
            this.description = cookingStep.getDescription();
        }
    }
}
