package com.example.backend.domain.shopping.dto;

import com.example.backend.domain.recipe.RecipeIngredient;
import lombok.Getter;

import java.math.BigDecimal;

// 장보기 리스트에 담기는 개별 부족 재료 (FR-30)
@Getter
public class ShoppingListItemResponse {

    private final Long ingredientId;
    private final String ingredientName;
    private final String categoryName;
    private final BigDecimal quantity;
    private final String unit;
    private final boolean inMyList;

    public ShoppingListItemResponse(RecipeIngredient recipeIngredient, boolean inMyList) {
        this.ingredientId = recipeIngredient.getIngredient().getIngredientId();
        this.ingredientName = recipeIngredient.getIngredient().getIngredientName();
        this.categoryName = recipeIngredient.getIngredient().getCategory() != null
                ? recipeIngredient.getIngredient().getCategory().getCategoryName()
                : "기타";
        this.quantity = recipeIngredient.getQuantity();
        this.unit = recipeIngredient.getUnit();
        this.inMyList = inMyList;
    }
}