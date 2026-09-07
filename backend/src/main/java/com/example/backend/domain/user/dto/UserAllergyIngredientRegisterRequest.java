package com.example.backend.domain.user.dto;

import com.example.backend.domain.user.UserAllergyIngredient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 마이페이지에서 알레르기/기피 재료를 직접 입력해 등록할 때 프론트가 보내는 데이터
public record UserAllergyIngredientRegisterRequest(
        @NotBlank String ingredientName,
        @NotNull UserAllergyIngredient.Type type
) {
}
