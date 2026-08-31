package com.example.backend.domain.notification;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 목록 (최신순, 최대 30개만 — 그 이상은 사실상 안 보므로 무한정 쌓아둘 필요 없음)
    List<Notification> findTop30ByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);

    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    // 여러 건을 한 번에 읽음 처리할 때, 하나씩 조회 후 저장하는 것보다 훨씬 빠름
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUserId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
