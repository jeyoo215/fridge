package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

// 재료 수정(수량/구매일/소비기한 변경) 요청
public record UserIngredientUpdateRequest(
        @NotNull(message = "수량을 입력해주세요.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        BigDecimal quantity,

        LocalDate purchaseDate,

        @NotNull LocalDate expirationDate
) {
}
