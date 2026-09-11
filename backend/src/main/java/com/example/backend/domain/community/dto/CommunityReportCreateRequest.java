package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CommunityReportCreateRequest(
        // CommunityReportService.VALID_REASONS 중 하나여야 함 (서비스에서 화이트리스트 검증)
        @NotBlank String reason
) {
}
