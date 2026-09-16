package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.ReportedTargetResponse;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityReportService {

    // 이 개수 이상 신고가 쌓이면 관리자 판단이 나기 전까지 다른 사람들 눈에 안 보이게 자동으로 숨긴다.
    private static final long HIDE_THRESHOLD = 5;

    // 프론트 신고 사유 select의 옵션과 정확히 일치해야 함
    private static final Set<String> VALID_REASONS = Set.of("스팸/광고", "욕설/혐오", "음란물", "허위정보", "기타");

    private static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";

    private final CommunityReportRepository communityReportRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostService communityPostService;
    private final CommunityPostCommentService communityPostCommentService;
    private final UserRepository userRepository;

    // 게시글 신고
    @Transactional
    public void reportPost(Long userId, Long postId, String reason) {
        requireLoggedIn(userId);
        validateReason(reason);

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        if (post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 글은 신고할 수 없습니다.");
        }

        saveReportIfNotDuplicate(CommunityReport.TargetType.POST, postId, userId, reason);

        if (communityReportRepository.countByTargetTypeAndTargetId(CommunityReport.TargetType.POST, postId) >= HIDE_THRESHOLD) {
            post.hide();
        }
    }

    // 댓글 신고
    @Transactional
    public void reportComment(Long userId, Long commentId, String reason) {
        requireLoggedIn(userId);
        validateReason(reason);

        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + commentId));
        if (comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 댓글은 신고할 수 없습니다.");
        }

        saveReportIfNotDuplicate(CommunityReport.TargetType.COMMENT, commentId, userId, reason);

        if (communityReportRepository.countByTargetTypeAndTargetId(CommunityReport.TargetType.COMMENT, commentId) >= HIDE_THRESHOLD) {
            comment.hide();
        }
    }

    private void requireLoggedIn(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    private void validateReason(String reason) {
        if (!VALID_REASONS.contains(reason)) {
            throw new IllegalArgumentException("올바른 신고 사유를 선택해주세요.");
        }
    }

    private void saveReportIfNotDuplicate(CommunityReport.TargetType targetType, Long targetId, Long userId, String reason) {
        boolean alreadyReported = communityReportRepository
                .findByTargetTypeAndTargetIdAndReporterUserId(targetType, targetId, userId)
                .isPresent();
        if (alreadyReported) {
            throw new IllegalArgumentException("이미 신고한 게시글/댓글입니다.");
        }
        communityReportRepository.save(CommunityReport.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporterUserId(userId)
                .reason(reason)
                .build());
    }

    // 관리자: 신고가 들어온 대상 목록 (신고 많은 순 + 최근순). 삭제된 대상은 deleted=true로 표시만 하고 남겨둔다
    // (관리자가 "신고 무시" 버튼으로 정리할 수 있게).
    public List<ReportedTargetResponse> getReportedTargets() {
        return communityReportRepository.findReportedTargetGroups().stream()
                .map(group -> toResponse(group.getTargetType(), group.getTargetId(),
                        group.getReportCount(), group.getLastReportedAt()))
                .toList();
    }

    private ReportedTargetResponse toResponse(CommunityReport.TargetType targetType, Long targetId,
                                               long reportCount, java.time.LocalDateTime lastReportedAt) {
        List<String> reasons = communityReportRepository.findReasonsByTarget(targetType, targetId);

        if (targetType == CommunityReport.TargetType.POST) {
            return communityPostRepository.findById(targetId)
                    .map(post -> new ReportedTargetResponse(
                            targetType, targetId, null, reportCount, reasons, lastReportedAt,
                            post.isHidden(), false, post.getTitle(), nicknameOf(post.getUserId())))
                    .orElseGet(() -> new ReportedTargetResponse(
                            targetType, targetId, null, reportCount, reasons, lastReportedAt,
                            false, true, "(삭제된 게시글)", UNKNOWN_NICKNAME));
        }
        return communityPostCommentRepository.findById(targetId)
                .map(comment -> new ReportedTargetResponse(
                        targetType, targetId, comment.getPost().getPostId(), reportCount, reasons, lastReportedAt,
                        comment.isHidden(), false, comment.getContent(), nicknameOf(comment.getUserId())))
                .orElseGet(() -> new ReportedTargetResponse(
                        targetType, targetId, null, reportCount, reasons, lastReportedAt,
                        false, true, "(삭제된 댓글)", UNKNOWN_NICKNAME));
    }

    private String nicknameOf(Long userId) {
        return userRepository.findById(userId).map(User::getNickname).orElse(UNKNOWN_NICKNAME);
    }

    // 관리자: 신고가 정당하다고 판단 — 대상을 삭제한다 (삭제 로직 자체가 그 대상의 신고 row도 같이 정리함)
    @Transactional
    public void resolveByDeleting(CommunityReport.TargetType targetType, Long targetId) {
        if (targetType == CommunityReport.TargetType.POST) {
            communityPostService.adminDelete(targetId);
        } else {
            communityPostCommentService.adminDelete(targetId);
        }
    }

    // 관리자: 신고가 부당하다고 판단 — 숨김 상태였다면 풀어주고, 쌓여있던 신고는 전부 지운다
    @Transactional
    public void resolveByDismissing(CommunityReport.TargetType targetType, Long targetId) {
        if (targetType == CommunityReport.TargetType.POST) {
            communityPostRepository.findById(targetId).ifPresent(CommunityPost::unhide);
        } else {
            communityPostCommentRepository.findById(targetId).ifPresent(CommunityPostComment::unhide);
        }
        communityReportRepository.deleteByTargetTypeAndTargetId(targetType, targetId);
    }
}
