package com.example.backend.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FridgeNameRequest(
        @NotBlank(message = "냉장고 이름을 입력해주세요.")
        @Size(max = 30, message = "냉장고 이름은 30자 이내로 입력해주세요.")
        String fridgeName
) {
}
