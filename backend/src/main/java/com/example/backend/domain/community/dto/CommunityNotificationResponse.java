package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityNotification;
import lombok.Getter;

import java.time.LocalDateTime;

// 알림 벨 드롭다운의 항목 하나
@Getter
public class CommunityNotificationResponse {

    private final Long notificationId;
    private final CommunityNotification.Type type;
    private final Long postId;
    private final Long commentId;
    private final String actorNickname; // 댓글/답글을 단 사람
    private final String postTitle;
    private final boolean read;
    private final LocalDateTime createdAt;

    public CommunityNotificationResponse(CommunityNotification entity, String actorNickname, String postTitle) {
        this.notificationId = entity.getNotificationId();
        this.type = entity.getType();
        this.postId = entity.getPostId();
        this.commentId = entity.getCommentId();
        this.actorNickname = actorNickname;
        this.postTitle = postTitle;
        this.read = entity.isRead();
        this.createdAt = entity.getCreatedAt();
    }
}
