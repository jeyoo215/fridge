package com.example.backend.domain.ingredient.dto;

import com.example.backend.domain.ingredient.Ingredient;
import lombok.Getter;

@Getter
public class IngredientSearchResponse {

    private final Long ingredientId;
    private final String ingredientName;
    private final String categoryName;
    private final Integer defaultShelfLifeDays; // 유통기한 자동 계산에 참고용 (등록 화면에서 기본값으로 미리 채워줄 때 사용)

    public IngredientSearchResponse(Ingredient entity) {
        this.ingredientId = entity.getIngredientId();
        this.ingredientName = entity.getIngredientName();
        this.categoryName = entity.getCategory() != null ? entity.getCategory().getCategoryName() : null;
        this.defaultShelfLifeDays = entity.getDefaultShelfLifeDays();
    }
}
