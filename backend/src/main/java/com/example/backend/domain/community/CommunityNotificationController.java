package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityNotificationResponse;
import com.example.backend.domain.community.dto.CommunityNotificationUnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community/notifications")
@RequiredArgsConstructor
public class CommunityNotificationController {

    private final CommunityNotificationService communityNotificationService;

    // 알림 벨 드롭다운 목록 (최신순)
    @GetMapping
    public List<CommunityNotificationResponse> getNotifications(@AuthenticationPrincipal Long userId) {
        return communityNotificationService.getNotifications(userId);
    }

    // 벨 아이콘 위 안 읽은 개수 배지
    @GetMapping("/unread-count")
    public CommunityNotificationUnreadCountResponse getUnreadCount(@AuthenticationPrincipal Long userId) {
        return new CommunityNotificationUnreadCountResponse(communityNotificationService.getUnreadCount(userId));
    }

    // 알림 한 개 읽음 처리 (알림 항목 클릭 시)
    @PostMapping("/{notificationId}/read")
    public void markRead(@PathVariable("notificationId") Long notificationId,
                          @AuthenticationPrincipal Long userId) {
        communityNotificationService.markRead(userId, notificationId);
    }

    // 전부 읽음 처리 ("모두 읽음" 버튼)
    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal Long userId) {
        communityNotificationService.markAllRead(userId);
    }
}
