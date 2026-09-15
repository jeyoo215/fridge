package com.example.backend.domain.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TargetIngredientSuggestionResponse(
        Long ingredientId,
        String ingredientName,
        BigDecimal quantity,
        String unit,
        LocalDate expirationDate,
        int suggestedDays
) {
}