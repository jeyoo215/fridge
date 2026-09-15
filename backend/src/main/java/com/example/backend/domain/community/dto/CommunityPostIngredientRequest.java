package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CommunityPostIngredientRequest(
        @NotNull Long ingredientId,
        @NotNull BigDecimal quantity,
        @NotBlank String unit
) {
}
