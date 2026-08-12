package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// 재료 수정(수량/구매일/소비기한/가격 변경) 요청
public record UserIngredientUpdateRequest(
        @NotNull(message = "수량을 입력해주세요.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        BigDecimal quantity,

        LocalDate purchaseDate,

        @NotNull LocalDate expirationDate,

        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        BigDecimal price
) {
}
