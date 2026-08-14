package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.RecipeCategory;
import lombok.Getter;

@Getter
public class RecipeCategoryResponse {

    private final Long categoryId;
    private final String categoryName;

    public RecipeCategoryResponse(RecipeCategory entity) {
        this.categoryId = entity.getCategoryId();
        this.categoryName = entity.getCategoryName();
    }
}
