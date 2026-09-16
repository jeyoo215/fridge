package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 게시글/댓글 신고. 신고 자체는 "처리 대기" 상태만 존재한다 — 관리자가 처리(삭제 또는 기각)하면
// 그 대상(targetType, targetId)에 대한 신고 row를 전부 지워버리므로 별도의 상태(status) 컬럼이 없다.
@Entity
@Table(name = "community_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    // targetType=POST면 CommunityPost.postId, COMMENT면 CommunityPostComment.commentId
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    // 고정된 사유 화이트리스트(REASONS) 중 하나. 프론트 select 옵션과 값이 일치해야 함.
    @Column(nullable = false, length = 20)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CommunityReport(TargetType targetType, Long targetId, Long reporterUserId, String reason) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reporterUserId = reporterUserId;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public enum TargetType {
        POST, COMMENT
    }
}
