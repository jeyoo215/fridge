package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityPostCommentResponse {

    private final Long commentId;
    private final Long userId;
    private final String content;
    private final LocalDateTime createdAt;

    public CommunityPostCommentResponse(CommunityPostComment entity) {
        this.commentId = entity.getCommentId();
        this.userId = entity.getUserId();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
    }
}
