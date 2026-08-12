package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CommunityPostIngredientRequest(
        @NotNull Long ingredientId,
        BigDecimal quantity,
        String unit
) {
}
