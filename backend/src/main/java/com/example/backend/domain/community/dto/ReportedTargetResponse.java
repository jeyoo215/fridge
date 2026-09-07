package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityReport;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 관리자 신고 목록 화면의 항목 하나. 같은 대상(targetType+targetId)에 대한 여러 신고를 하나로 묶어서 보여준다.
@Getter
public class ReportedTargetResponse {

    private final CommunityReport.TargetType targetType;
    private final Long targetId;
    private final Long postId; // targetType=COMMENT일 때, 그 댓글이 달린 게시글로 이동하기 위함
    private final long reportCount;
    private final List<String> reasons;
    private final LocalDateTime lastReportedAt;
    private final boolean hidden;
    private final boolean deleted; // 이미 삭제된 대상(신고 row만 남아있는 경우) 방어적으로 표시
    private final String preview; // 게시글이면 제목, 댓글이면 내용 일부
    private final String authorNickname;

    public ReportedTargetResponse(CommunityReport.TargetType targetType, Long targetId, Long postId,
                                   long reportCount, List<String> reasons, LocalDateTime lastReportedAt,
                                   boolean hidden, boolean deleted, String preview, String authorNickname) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.postId = postId;
        this.reportCount = reportCount;
        this.reasons = reasons;
        this.lastReportedAt = lastReportedAt;
        this.hidden = hidden;
        this.deleted = deleted;
        this.preview = preview;
        this.authorNickname = authorNickname;
    }
}
