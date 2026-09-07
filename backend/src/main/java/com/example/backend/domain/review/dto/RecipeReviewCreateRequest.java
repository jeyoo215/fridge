package com.example.backend.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecipeReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        String content
) {
}