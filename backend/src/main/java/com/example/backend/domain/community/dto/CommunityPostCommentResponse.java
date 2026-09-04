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
    private final long likeCount;
    private final boolean liked; // 지금 조회한 사용자가 이 댓글에 공감했는지 (비로그인이면 항상 false)

    public CommunityPostCommentResponse(CommunityPostComment entity, String nickname, boolean liked) {
        this.commentId = entity.getCommentId();
        this.userId = entity.getUserId();
        this.nickname = nickname;
        this.content = entity.getContent();
        this.parentCommentId = entity.getParentCommentId();
        this.createdAt = entity.getCreatedAt();
        this.likeCount = entity.getLikeCount();
        this.liked = liked;
    }
}
