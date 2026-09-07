package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

    // 같은 유저가 같은 대상을 중복 신고하는 것 방지
    Optional<CommunityReport> findByTargetTypeAndTargetIdAndReporterUserId(
            CommunityReport.TargetType targetType, Long targetId, Long reporterUserId);

    long countByTargetTypeAndTargetId(CommunityReport.TargetType targetType, Long targetId);

    List<CommunityReport> findByTargetTypeAndTargetId(CommunityReport.TargetType targetType, Long targetId);

    // 관리자 목록: 아직 처리 안 된(=신고 row가 남아있는) 대상들을 (targetType, targetId)별로 묶어서,
    // 신고 개수 많은 순 + 최근 신고순으로 보여준다.
    @Query("""
        SELECT r.targetType AS targetType, r.targetId AS targetId,
               COUNT(r) AS reportCount, MAX(r.createdAt) AS lastReportedAt
        FROM CommunityReport r
        GROUP BY r.targetType, r.targetId
        ORDER BY COUNT(r) DESC, MAX(r.createdAt) DESC
        """)
    List<ReportedTargetGroup> findReportedTargetGroups();

    // 관리자 처리(삭제/기각) 시 해당 대상의 신고 row를 전부 지움
    void deleteByTargetTypeAndTargetId(CommunityReport.TargetType targetType, Long targetId);

    // 게시글/댓글 자체가 지워질 때(작성자 본인 삭제 등) 남은 신고 row도 같이 정리하기 위함
    void deleteByTargetTypeAndTargetIdIn(CommunityReport.TargetType targetType, List<Long> targetIds);

    interface ReportedTargetGroup {
        CommunityReport.TargetType getTargetType();
        Long getTargetId();
        Long getReportCount();
        java.time.LocalDateTime getLastReportedAt();
    }

    @Query("SELECT r.reason FROM CommunityReport r WHERE r.targetType = :targetType AND r.targetId = :targetId")
    List<String> findReasonsByTarget(@Param("targetType") CommunityReport.TargetType targetType,
                                      @Param("targetId") Long targetId);
}
