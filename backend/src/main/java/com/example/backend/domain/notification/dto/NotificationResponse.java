package com.example.backend.domain.notification.dto;

import com.example.backend.domain.notification.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final Long notificationId;
    private final String type;
    private final String message; // 화면에 그대로 보여줄 문구 ("OO님이 회원님의 글에 댓글을 남겼어요")
    private final Long postId;
    private final Long commentId;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification n) {
        this.notificationId = n.getNotificationId();
        this.type = n.getType().name();
        this.message = buildMessage(n);
        this.postId = n.getPostId();
        this.commentId = n.getCommentId();
        this.isRead = n.isRead();
        this.createdAt = n.getCreatedAt();
    }

    private String buildMessage(Notification n) {
        String action = n.getType() == Notification.NotificationType.NEW_REPLY
                ? "회원님의 댓글에 답글을 남겼어요"
                : "회원님의 글에 댓글을 남겼어요";
        return String.format("%s님이 \"%s\"에 %s", n.getActorNickname(), truncate(n.getPostTitle()), action);
    }

    private String truncate(String title) {
        return title.length() > 20 ? title.substring(0, 20) + "…" : title;
    }
}
