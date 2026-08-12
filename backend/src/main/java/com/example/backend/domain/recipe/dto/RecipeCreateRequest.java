package com.example.backend.domain.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

// 레시피 등록 요청 DTO (FR-24, FR-22 조리도구)
public record RecipeCreateRequest(
        Long categoryId,                  // 레시피 카테고리 id
        String recipeName,                // 레시피 이름
        Integer cookingTimeMinutes,       // 조리 시간(분)
        String difficulty,                // 난이도
        String imageUrl,                  // 대표 이미지 url
        List<IngredientItem> ingredients, // 필요 재료 목록
        List<StepItem> steps,             // 조리 순서 목록
        List<Long> toolIds,               // 필요 조리도구 id 목록 (FR-22, cooking_tool 참조)
        String source                     // 출처 (예: "커뮤니티" — 커뮤니티 글 승격으로 생성된 경우)
) {
    // 재료 항목 (재료 id + 수량 + 단위)
    public record IngredientItem(
            Long ingredientId,
            BigDecimal quantity,
            String unit
    ) {
    }

    // 조리 단계 항목 (순서 + 설명)
    public record StepItem(
            Integer stepOrder,
            String description
    ) {
    }
}
