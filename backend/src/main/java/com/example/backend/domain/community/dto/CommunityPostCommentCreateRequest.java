package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityPostCommentCreateRequest(
        @NotBlank @Size(max = 500) String content
) {
}
