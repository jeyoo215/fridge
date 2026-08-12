package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// 재료 등록 화면에서 "등록하기" 눌렀을 때 프론트가 보내는 데이터
public record UserIngredientRegisterRequest(
        @NotNull Long ingredientId,

        @NotNull(message = "수량을 입력해주세요.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        BigDecimal quantity,

        String unit,
        LocalDate purchaseDate,

        @NotNull LocalDate expirationDate,

        // 선택 입력. 입력 안 하면 통계에서 평균 추정치로 대체됨. 입력했다면 0 이상이어야 함(음수 방지).
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        BigDecimal price
) {
}
