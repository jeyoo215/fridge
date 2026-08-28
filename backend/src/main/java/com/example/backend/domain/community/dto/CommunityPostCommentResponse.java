package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityPostCommentResponse {

    private final Long commentId;
    private final Long userId;
    private final String nickname;
    private final String content;
    private final Long parentCommentId;
    private final LocalDateTime createdAt;

    public CommunityPostCommentResponse(CommunityPostComment entity, String nickname) {
        this.commentId = entity.getCommentId();
        this.userId = entity.getUserId();
        this.nickname = nickname;
        this.content = entity.getContent();
        this.parentCommentId = entity.getParentCommentId();
        this.createdAt = entity.getCreatedAt();
    }
}
