package com.example.backend.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityPostCommentCreateRequest(
        @NotBlank @Size(max = 500) String content,
        // 대댓글이면 원댓글 id, 일반 댓글이면 null (선택값)
        Long parentCommentId
) {
}
