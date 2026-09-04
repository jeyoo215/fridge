package com.example.backend.domain.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChallengeStartRequest(
        @NotNull @Min(1) Integer days,
        @NotBlank String type,               // "FRIDGE_CLEAN" | "TARGET_INGREDIENT"
        List<Long> targetIngredientIds        // TARGET_INGREDIENT일 때만 필수
) {
}