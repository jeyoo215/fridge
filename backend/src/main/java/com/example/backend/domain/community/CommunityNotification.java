package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 커뮤니티 알림: 내가 쓴 글에 댓글이 달렸을 때(POST_COMMENT), 내가 쓴 댓글에 답글이 달렸을 때
// (COMMENT_REPLY) 받는 사람(recipientUserId) 앞으로 하나씩 쌓인다.
// 자기 글에 자기가 댓글을 단 경우처럼 actor==recipient면 애초에 생성하지 않는다
// (CommunityNotificationService.notifyOnComment).
@Entity
@Table(name = "community_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // 알림을 받는 사람 (게시글 작성자 또는 원댓글/대댓글 작성자)
    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    // 알림을 발생시킨 사람 (새로 댓글/답글을 단 사람)
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;

    // 이동할 게시글 id (항상 존재)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 새로 달린 댓글/답글의 id. 그 댓글(들)이 나중에 지워지면 CommunityNotificationRepository의
    // deleteByCommentIdIn으로 이 알림도 같이 정리된다 (CommunityPostCommentService.deleteCommentInternal).
    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "read_status", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CommunityNotification(Long recipientUserId, Long actorUserId, Type type, Long postId, Long commentId) {
        this.recipientUserId = recipientUserId;
        this.actorUserId = actorUserId;
        this.type = type;
        this.postId = postId;
        this.commentId = commentId;
        this.createdAt = LocalDateTime.now();
    }

    public void markRead() {
        this.read = true;
    }

    public enum Type {
        POST_COMMENT,   // 내가 쓴 게시글에 (원)댓글이 달림
        COMMENT_REPLY   // 내가 쓴 댓글/대댓글에 답글이 달림
    }
}
