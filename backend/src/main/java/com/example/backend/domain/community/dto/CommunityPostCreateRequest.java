package com.example.backend.domain.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// 커뮤니티 게시글 작성 요청. 제목 + 섹션(소제목/본문/이미지) 최소 1개 이상.
public record CommunityPostCreateRequest(
        @NotBlank String title,
        @NotEmpty @Valid List<CommunityPostSectionRequest> sections
) {
}
