package com.example.backend.domain.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 댓글/답글 알림. 닉네임·게시글 제목은 알림이 만들어진 시점 값을 그대로 저장해둔다
// (나중에 닉네임이 바뀌거나 게시글이 지워져도, 알림 목록에 남는 문구는 그때 그대로 보여야 하니까).
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // 알림을 받는 사람 (게시글 작성자 또는 원댓글 작성자)
    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    // 알림을 발생시킨 사람 (댓글/답글을 단 사람)
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "actor_nickname", nullable = false, length = 50)
    private String actorNickname;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "post_title", nullable = false, length = 200)
    private String postTitle;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long recipientUserId, Long actorUserId, String actorNickname,
                         Long postId, String postTitle, Long commentId, NotificationType type) {
        this.recipientUserId = recipientUserId;
        this.actorUserId = actorUserId;
        this.actorNickname = actorNickname;
        this.postId = postId;
        this.postTitle = postTitle;
        this.commentId = commentId;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public enum NotificationType {
        NEW_COMMENT, // 내가 쓴 글에 댓글이 달림
        NEW_REPLY    // 내가 쓴 댓글에 답글이 달림
    }
}
