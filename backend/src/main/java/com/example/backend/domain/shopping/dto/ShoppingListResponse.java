package com.example.backend.domain.shopping.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ShoppingListResponse {

    private final Long recipeId;
    private final String recipeName;
    private final List<ShoppingListItemResponse> missingIngredients;

    public ShoppingListResponse(Long recipeId, String recipeName, List<ShoppingListItemResponse> missingIngredients) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.missingIngredients = missingIngredients;
    }
}