package com.example.backend.domain.badge;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 사용자별 챌린지 성공 스트릭 현황 (1인당 1row)
@Entity
@Table(name = "streak_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreakRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "streak_id")
    private Long streakId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    // 누적 챌린지 성공 횟수 (CHALLENGE_SUCCESS_COUNT 조건 판정용)
    @Column(name = "total_success_count", nullable = false)
    private int totalSuccessCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public StreakRecord(Long userId) {
        this.userId = userId;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalSuccessCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseOnSuccess() {
        this.currentStreak += 1;
        this.totalSuccessCount += 1;
        if (this.currentStreak > this.longestStreak) {
            this.longestStreak = this.currentStreak;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void resetOnFail() {
        this.currentStreak = 0;
        this.updatedAt = LocalDateTime.now();
    }
}