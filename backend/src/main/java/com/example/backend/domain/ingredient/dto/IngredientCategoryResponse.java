package com.example.backend.domain.ingredient.dto;

import com.example.backend.domain.ingredient.IngredientCategory;
import lombok.Getter;

@Getter
public class IngredientCategoryResponse {

    private final Long categoryId;
    private final String categoryName;

    public IngredientCategoryResponse(IngredientCategory entity) {
        this.categoryId = entity.getCategoryId();
        this.categoryName = entity.getCategoryName();
    }
}
