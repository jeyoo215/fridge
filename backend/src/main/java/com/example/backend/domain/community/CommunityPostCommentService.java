package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCommentCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostCommentResponse;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostCommentService {

    private static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";
    private static final String COMMENT_NOT_FOUND = "존재하지 않는 댓글입니다. id=";

    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityReportRepository communityReportRepository;
    private final UserRepository userRepository;

    // 댓글/대댓글 등록. parentCommentId가 있으면 대댓글이며, 대댓글의 대댓글(2단계)까지는 허용하되
    // 그 이상은 막는다 (원댓글=0단계, 대댓글=1단계, 대댓글의 댓글=2단계까지만 — 그 이상 중첩되면
    // 화면에서 답글 스레드를 알아보기 어려워짐).
    @Transactional
    public Long create(Long userId, Long postId, CommunityPostCommentCreateRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        Long parentCommentId = request.parentCommentId();
        if (parentCommentId != null) {
            CommunityPostComment parent = communityPostCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND + parentCommentId));
            if (!parent.getPost().getPostId().equals(postId)) {
                throw new IllegalArgumentException("다른 게시글의 댓글에는 답글을 달 수 없습니다.");
            }
            if (isReplyToReply(parent)) {
                throw new IllegalArgumentException("대댓글의 댓글에는 답글을 달 수 없습니다.");
            }
        }

        CommunityPostComment comment = CommunityPostComment.builder()
                .post(post)
                .userId(userId)
                .content(request.content())
                .parentCommentId(parentCommentId)
                .build();

        return communityPostCommentRepository.save(comment).getCommentId();
    }

    // parent가 이미 "대댓글의 댓글"(2단계)인지 확인. 원댓글(0단계)에 달린 대댓글(1단계)까지는
    // 답글을 더 달 수 있지만, 그 밑(2단계)에는 더 이상 답글을 못 달게 막기 위함.
    private boolean isReplyToReply(CommunityPostComment parent) {
        Long grandParentId = parent.getParentCommentId();
        if (grandParentId == null) {
            return false; // parent 자체가 원댓글(0단계)
        }
        CommunityPostComment grandParent = communityPostCommentRepository.findById(grandParentId)
                .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND + grandParentId));
        return grandParent.getParentCommentId() != null; // grandParent도 대댓글이면 parent는 이미 2단계
    }

    // 게시글의 댓글 목록 (등록순, 대댓글도 같이 평탄한 목록으로 반환 — parentCommentId로 화면에서 묶어서 보여줌)
    // 신고 누적으로 hidden=true가 된 댓글은 목록에서 제외한다 (관리자 검토 전까지).
    public List<CommunityPostCommentResponse> getComments(Long postId) {
        List<CommunityPostComment> comments = communityPostCommentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .filter(comment -> !comment.isHidden())
                .toList();

        Map<Long, String> nicknamesByUserId = userRepository.findAllById(
                comments.stream().map(CommunityPostComment::getUserId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getUserId, User::getNickname));

        return comments.stream()
                .map(comment -> new CommunityPostCommentResponse(
                        comment, nicknamesByUserId.getOrDefault(comment.getUserId(), UNKNOWN_NICKNAME)))
                .toList();
    }

    // 댓글 삭제 (본인 댓글만 가능)
    @Transactional
    public void delete(Long userId, Long commentId) {
        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        deleteCommentInternal(comment);
    }

    // 관리자 전용: 신고 처리로 댓글을 강제 삭제 (작성자 소유 여부와 무관, CommunityReportService에서 호출)
    @Transactional
    public void adminDelete(Long commentId) {
        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND + commentId));
        deleteCommentInternal(comment);
    }

    // 밑에 달린 답글(대댓글, 대댓글의 댓글)까지 전부 같이 지우고, 지워지는 댓글(들)에 쌓여있던
    // 신고 row도 정리한다 (안 지우면 부모 없는 답글이 고아로 남거나, 존재하지 않는 댓글을 가리키는
    // 신고가 남는다). 최대 깊이가 2단계로 고정돼있어 재귀 호출 비용은 무시할 만하다.
    private void deleteCommentInternal(CommunityPostComment comment) {
        Long commentId = comment.getCommentId();
        List<Long> descendantIds = collectDescendantIds(commentId);
        if (!descendantIds.isEmpty()) {
            communityReportRepository.deleteByTargetTypeAndTargetIdIn(CommunityReport.TargetType.COMMENT, descendantIds);
            communityPostCommentRepository.deleteAllById(descendantIds);
        }
        communityReportRepository.deleteByTargetTypeAndTargetId(CommunityReport.TargetType.COMMENT, commentId);
        communityPostCommentRepository.delete(comment);
    }

    // parentId에 달린 답글들의 id를 그 밑의 답글까지 재귀적으로 전부 모은다.
    private List<Long> collectDescendantIds(Long parentId) {
        List<CommunityPostComment> children = communityPostCommentRepository.findByParentCommentId(parentId);
        List<Long> ids = new ArrayList<>();
        for (CommunityPostComment child : children) {
            ids.add(child.getCommentId());
            ids.addAll(collectDescendantIds(child.getCommentId()));
        }
        return ids;
    }
}
