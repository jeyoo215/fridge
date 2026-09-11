package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;

// 재료 마스터에 없는 재료를 사용자가 직접 새로 등록할 때 쓰는 요청
// isSeasoning=true(조미료)면 categoryId 없이 등록 가능. false(재료)면 categoryId 필수.
public record IngredientCreateRequest(
        @NotBlank(message = "재료 이름을 입력해주세요.")
        String ingredientName,

        Long categoryId, // 조미료면 null 가능, 재료면 필수 (Service에서 검증)

        String storageMethod, // "냉장"/"냉동"/"실온" 중 하나, 선택 입력

        boolean isSeasoning // true: 조미료, false: 재료 (기본값 false)
) {
}
