package com.example.backend.domain.notification;

import com.example.backend.domain.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 새 댓글/답글이 달렸을 때 호출됨 (CommunityPostCommentService에서 댓글 저장 직후 호출).
    // 자기 글/자기 댓글에 자기가 댓글 달았을 때는 알림을 안 보낸다 (본인한테 알릴 필요 없으니까).
    @Transactional
    public void notifyNewComment(Long recipientUserId, Long actorUserId, String actorNickname,
                                  Long postId, String postTitle, Long commentId,
                                  Notification.NotificationType type) {
        if (recipientUserId.equals(actorUserId)) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .actorNickname(actorNickname)
                .postId(postId)
                .postTitle(postTitle)
                .commentId(commentId)
                .type(type)
                .build());
    }

    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findTop30ByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    public Map<String, Long> getUnreadCount(Long userId) {
        return Map.of("count", notificationRepository.countByRecipientUserIdAndIsReadFalse(userId));
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다. id=" + notificationId));
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
