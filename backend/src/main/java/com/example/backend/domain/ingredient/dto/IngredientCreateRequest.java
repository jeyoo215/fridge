package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 재료 마스터에 없는 재료를 사용자가 직접 새로 등록할 때 쓰는 요청
public record IngredientCreateRequest(
        @NotBlank(message = "재료 이름을 입력해주세요.")
        String ingredientName,

        @NotNull(message = "카테고리를 선택해주세요.")
        Long categoryId,

        String storageMethod // "냉장"/"냉동"/"실온" 중 하나, 선택 입력
) {
}
