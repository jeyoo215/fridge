package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.ReportedTargetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 관리자 전용 신고 처리. SecurityConfig에서 /api/v1/admin/** 는 hasRole("ADMIN")으로 보호됨.
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final CommunityReportService communityReportService;

    // 신고가 들어온 게시글/댓글 목록 (신고 많은 순)
    @GetMapping
    public List<ReportedTargetResponse> getReportedTargets() {
        return communityReportService.getReportedTargets();
    }

    // 신고가 정당함 — 대상을 삭제
    @PostMapping("/{targetType}/{targetId}/delete")
    public void resolveByDeleting(@PathVariable("targetType") CommunityReport.TargetType targetType,
                                   @PathVariable("targetId") Long targetId) {
        communityReportService.resolveByDeleting(targetType, targetId);
    }

    // 신고가 부당함 — 숨김 해제하고 신고 목록에서 제거
    @PostMapping("/{targetType}/{targetId}/dismiss")
    public void resolveByDismissing(@PathVariable("targetType") CommunityReport.TargetType targetType,
                                     @PathVariable("targetId") Long targetId) {
        communityReportService.resolveByDismissing(targetType, targetId);
    }
}
