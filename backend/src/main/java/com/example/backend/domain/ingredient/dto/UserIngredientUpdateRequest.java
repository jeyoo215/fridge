package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// 재료 수정(수량/유통기한 변경) 요청
public record UserIngredientUpdateRequest(
        @NotNull BigDecimal quantity,
        @NotNull LocalDate expirationDate
) {
}
