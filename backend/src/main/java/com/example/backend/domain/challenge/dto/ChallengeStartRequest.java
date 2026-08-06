package com.example.backend.domain.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChallengeStartRequest(
        @NotNull @Min(1) Integer days // 며칠짜리 챌린지인지 (예: 7일)
) {
}