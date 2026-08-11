package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// 재료 등록 화면에서 "등록하기" 눌렀을 때 프론트가 보내는 데이터
public record UserIngredientRegisterRequest(
        @NotNull Long ingredientId,
        @NotNull BigDecimal quantity,
        String unit,
        LocalDate purchaseDate,
        @NotNull LocalDate expirationDate,
        BigDecimal price // 선택 입력. 입력 안 하면 통계에서 평균 추정치로 대체됨
) {
}