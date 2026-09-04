package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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

        @NotNull LocalDate expirationDate
) {
}
