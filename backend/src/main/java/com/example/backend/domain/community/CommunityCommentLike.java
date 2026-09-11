package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 커뮤니티 댓글(대댓글 포함) 공감 (사용자 1명이 댓글 1개에 공감 최대 1번). CommunityPostLike와 동일한 패턴.
@Entity
@Table(name = "community_comment_like", uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long commentLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommunityPostComment comment;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CommunityCommentLike(CommunityPostComment comment, Long userId) {
        this.comment = comment;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
