package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.Recipe;
import lombok.Getter;

import java.util.List;
import java.util.Comparator;

// 레시피 상세 화면용 응답 DTO (FR-24: 재료, 조리순서, 조리시간)
@Getter
public class RecipeDetailResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final String categoryName;
    private final List<IngredientItem> ingredients;
    private final List<StepItem> steps;

    public RecipeDetailResponse(Recipe entity) {
        this.recipeId = entity.getRecipeId();
        this.recipeName = entity.getRecipeName();
        this.imageUrl = entity.getImageUrl();
        this.cookingTimeMinutes = entity.getCookingTimeMinutes();
        this.difficulty = entity.getDifficulty();
        this.categoryName = entity.getCategory().getCategoryName();

        this.ingredients = entity.getRecipeIngredients().stream()
                .map(IngredientItem::new)
                .toList();

        this.steps = entity.getCookingSteps().stream()
                .sorted(Comparator.comparingInt(step -> step.getStepOrder()))
                .map(StepItem::new)
                .toList();
    }

    @Getter
    public static class IngredientItem {
        private final String ingredientName;
        private final String quantity;
        private final String unit;

        public IngredientItem(com.example.backend.domain.recipe.RecipeIngredient entity) {
            this.ingredientName = entity.getIngredient().getIngredientName();
            this.quantity = entity.getQuantity() != null ? entity.getQuantity().stripTrailingZeros().toPlainString() : null;
            this.unit = entity.getUnit();
        }
    }

    @Getter
    public static class StepItem {
        private final int stepOrder;
        private final String description;

        public StepItem(com.example.backend.domain.recipe.CookingStep entity) {
            this.stepOrder = entity.getStepOrder();
            this.description = entity.getDescription();
        }
    }
}