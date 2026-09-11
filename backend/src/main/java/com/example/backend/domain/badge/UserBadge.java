package com.example.backend.domain.badge;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 사용자가 실제로 획득한 뱃지
@Entity
@Table(name = "user_badge", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long userBadgeId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    @Builder
    public UserBadge(Long userId, Badge badge) {
        this.userId = userId;
        this.badge = badge;
        this.earnedAt = LocalDateTime.now();
    }
}