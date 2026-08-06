package com.example.backend.domain.recipe.dto;

import com.example.backend.domain.recipe.RecipeCategory;

// 레시피 카테고리 응답 DTO (등록 화면 드롭다운용)
public record RecipeCategoryResponse(
        Long categoryId,
        String categoryName
) {
    public RecipeCategoryResponse(RecipeCategory category) {
        this(category.getCategoryId(), category.getCategoryName());
    }
}
