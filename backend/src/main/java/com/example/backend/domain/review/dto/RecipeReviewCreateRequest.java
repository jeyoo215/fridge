package com.example.backend.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecipeReviewCreateRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank(message = "후기 내용을 입력해주세요.")
        @Size(min = 3, max = 500, message = "후기는 3자 이상 입력해주세요.")
        String content
) {
}