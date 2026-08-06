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

    public ShoppingListItemResponse(RecipeIngredient entity) {
        this.ingredientId = entity.getIngredient().getIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.categoryName = entity.getIngredient().getCategory() != null
                ? entity.getIngredient().getCategory().getCategoryName()
                : null;
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
    }
}