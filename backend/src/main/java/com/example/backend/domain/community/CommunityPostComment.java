package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 커뮤니티 게시글 댓글
@Entity
@Table(name = "community_post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String content;

    // 대댓글이면 원댓글(최상위 댓글)의 id, 일반 댓글이면 null.
    // 대댓글에는 다시 대댓글을 못 달게 해서(CommunityPostCommentService.create) 항상 1단계 깊이만 존재한다.
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 신고가 임계치 이상 누적되면 자동으로 true가 되어 댓글 목록에서 안 보이게 된다.
    // 관리자가 신고를 무시(기각)하면 다시 false로 풀린다.
    @Column(name = "hidden", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean hidden;

    @Builder
    public CommunityPostComment(CommunityPost post, Long userId, String content, Long parentCommentId) {
        this.post = post;
        this.userId = userId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = LocalDateTime.now();
    }

    // 신고 누적/관리자 처리 시 CommunityReportService 전용
    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }
}
