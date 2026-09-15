package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityNotificationResponse;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityNotificationService {

    // 벨 드롭다운에 보여줄 최대 개수 (그 이상은 마이페이지 등 별도 화면이 필요하지만 지금은 범위 밖)
    private static final int MAX_NOTIFICATIONS = 30;
    private static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";
    private static final String UNKNOWN_POST_TITLE = "삭제된 게시글";

    private final CommunityNotificationRepository communityNotificationRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    // 댓글/답글이 새로 달렸을 때 호출 (CommunityPostCommentService.create 전용).
    // parent가 없으면(원댓글) 게시글 작성자에게, 있으면(답글) 그 댓글 작성자에게 알림을 남긴다.
    // 자기 글/자기 댓글에 스스로 댓글을 단 경우는 알림을 만들지 않는다.
    @Transactional
    public void notifyOnComment(CommunityPostComment newComment, CommunityPost post, CommunityPostComment parent) {
        Long actorUserId = newComment.getUserId();
        Long recipientUserId = parent != null ? parent.getUserId() : post.getUserId();
        CommunityNotification.Type type = parent != null
                ? CommunityNotification.Type.COMMENT_REPLY
                : CommunityNotification.Type.POST_COMMENT;

        if (recipientUserId == null || recipientUserId.equals(actorUserId)) {
            return;
        }

        communityNotificationRepository.save(CommunityNotification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .type(type)
                .postId(post.getPostId())
                .commentId(newComment.getCommentId())
                .build());
    }

    // 알림 벨 드롭다운 목록 (최신순 최대 MAX_NOTIFICATIONS개)
    public List<CommunityNotificationResponse> getNotifications(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        List<CommunityNotification> notifications = communityNotificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, MAX_NOTIFICATIONS));
        if (notifications.isEmpty()) {
            return List.of();
        }

        Set<Long> actorIds = notifications.stream().map(CommunityNotification::getActorUserId).collect(Collectors.toSet());
        Map<Long, String> nicknamesByUserId = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getNickname));

        Set<Long> postIds = notifications.stream().map(CommunityNotification::getPostId).collect(Collectors.toSet());
        Map<Long, String> titlesByPostId = communityPostRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(CommunityPost::getPostId, CommunityPost::getTitle));

        return notifications.stream()
                .map(n -> new CommunityNotificationResponse(
                        n,
                        nicknamesByUserId.getOrDefault(n.getActorUserId(), UNKNOWN_NICKNAME),
                        titlesByPostId.getOrDefault(n.getPostId(), UNKNOWN_POST_TITLE)))
                .toList();
    }

    public long getUnreadCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return communityNotificationRepository.countByRecipientUserIdAndReadFalse(userId);
    }

    // 알림 한 개 읽음 처리 (내 알림만)
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommunityNotification notification = communityNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다. id=" + notificationId));
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }
        notification.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        communityNotificationRepository.markAllReadByRecipientUserId(userId);
    }
}
