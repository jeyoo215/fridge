package com.example.backend.domain.notification;

import com.example.backend.domain.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getMyNotifications(@AuthenticationPrincipal Long userId) {
        return notificationService.getMyNotifications(userId);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@AuthenticationPrincipal Long userId, @PathVariable("notificationId") Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
