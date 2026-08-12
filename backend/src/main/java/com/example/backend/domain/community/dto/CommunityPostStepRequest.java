package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CommunityPostStepRequest(
        @NotBlank String description
) {
}
