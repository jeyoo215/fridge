package com.example.backend.domain.badge.dto;

import com.example.backend.domain.badge.StreakRecord;
import lombok.Getter;

@Getter
public class StreakResponse {
    private final int currentStreak;
    private final int longestStreak;
    private final int totalSuccessCount;

    public StreakResponse(StreakRecord entity) {
        this.currentStreak = entity.getCurrentStreak();
        this.longestStreak = entity.getLongestStreak();
        this.totalSuccessCount = entity.getTotalSuccessCount();
    }

    private StreakResponse(int currentStreak, int longestStreak, int totalSuccessCount) {
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.totalSuccessCount = totalSuccessCount;
    }

    // 아직 챌린지 성공 이력이 없는 유저용 기본값
    public static StreakResponse empty() {
        return new StreakResponse(0, 0, 0);
    }
}