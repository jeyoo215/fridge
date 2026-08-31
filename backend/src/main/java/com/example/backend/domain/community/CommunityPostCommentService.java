package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCommentCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostCommentResponse;
import com.example.backend.domain.notification.Notification;
import com.example.backend.domain.notification.NotificationService;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostCommentService {

    private static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";

    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // 댓글/답글 알림 발송용 (나경님 notification 도메인)

    // 댓글/대댓글 등록. parentCommentId가 있으면 대댓글이며, 대댓글에는 다시 대댓글을 달 수 없다
    // (1단계 깊이만 허용 — 그 이상 중첩되면 화면에서 답글 스레드를 알아보기 어려워짐).
    @Transactional
    public Long create(Long userId, Long postId, CommunityPostCommentCreateRequest request) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        Long parentCommentId = request.parentCommentId();
        if (parentCommentId != null) {
            CommunityPostComment parent = communityPostCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + parentCommentId));
            if (!parent.getPost().getPostId().equals(postId)) {
                throw new IllegalArgumentException("다른 게시글의 댓글에는 답글을 달 수 없습니다.");
            }
            if (parent.getParentCommentId() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
            }
        }

        CommunityPostComment comment = CommunityPostComment.builder()
                .post(post)
                .userId(userId)
                .content(request.content())
                .parentCommentId(parentCommentId)
                .build();

        Long commentId = communityPostCommentRepository.save(comment).getCommentId();

        // 댓글/답글 알림 발송: 답글이면 원댓글 작성자에게, 일반 댓글이면 게시글 작성자에게.
        // (알림 도메인 쪽에서 자기 자신에게는 알림을 안 보내도록 이미 처리하고 있음)
        String actorNickname = userRepository.findById(userId)
                .map(User::getNickname)
                .orElse(UNKNOWN_NICKNAME);
        if (parentCommentId != null) {
            CommunityPostComment parentComment = communityPostCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + parentCommentId));
            notificationService.notifyNewComment(parentComment.getUserId(), userId, actorNickname,
                    postId, post.getTitle(), commentId, Notification.NotificationType.NEW_REPLY);
        } else {
            notificationService.notifyNewComment(post.getUserId(), userId, actorNickname,
                    postId, post.getTitle(), commentId, Notification.NotificationType.NEW_COMMENT);
        }

        return commentId;
    }

    // 게시글의 댓글 목록 (등록순, 대댓글도 같이 평탄한 목록으로 반환 — parentCommentId로 화면에서 묶어서 보여줌)
    public List<CommunityPostCommentResponse> getComments(Long postId) {
        List<CommunityPostComment> comments = communityPostCommentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);

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
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 원댓글이면 밑에 달린 대댓글도 같이 지운다 (안 지우면 부모 없는 대댓글이 고아로 남음)
        if (comment.getParentCommentId() == null) {
            communityPostCommentRepository.deleteByParentCommentId(comment.getCommentId());
        }
        communityPostCommentRepository.delete(comment);
    }
}
