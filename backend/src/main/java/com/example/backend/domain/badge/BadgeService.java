package com.example.backend.domain.badge;

import com.example.backend.domain.badge.dto.BadgeResponse;
import com.example.backend.domain.badge.dto.StreakResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final StreakRecordRepository streakRecordRepository;

    // 챌린지 성공 시 호출: 스트릭 갱신 + 조건 충족 뱃지 자동 지급 (FR-42)
    @Transactional
    public void onChallengeSuccess(Long userId) {
        StreakRecord streak = streakRecordRepository.findByUserId(userId)
                .orElseGet(() -> streakRecordRepository.save(StreakRecord.builder().userId(userId).build()));

        streak.increaseOnSuccess();
        grantEligibleBadges(userId, streak);
    }

    // 챌린지 실패 시 호출: 연속 스트릭만 초기화 (누적 성공 횟수는 유지)
    @Transactional
    public void onChallengeFailed(Long userId) {
        streakRecordRepository.findByUserId(userId)
                .ifPresent(StreakRecord::resetOnFail);
    }

    private void grantEligibleBadges(Long userId, StreakRecord streak) {
        badgeRepository.findByConditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT).stream()
                .filter(badge -> streak.getTotalSuccessCount() >= badge.getConditionValue())
                .forEach(badge -> grantIfNotAlready(userId, badge));

        badgeRepository.findByConditionType(Badge.ConditionType.STREAK_COUNT).stream()
                .filter(badge -> streak.getCurrentStreak() >= badge.getConditionValue())
                .forEach(badge -> grantIfNotAlready(userId, badge));
    }

    private void grantIfNotAlready(Long userId, Badge badge) {
        if (!userBadgeRepository.existsByUserIdAndBadge_BadgeId(userId, badge.getBadgeId())) {
            userBadgeRepository.save(UserBadge.builder().userId(userId).badge(badge).build());
        }
    }

    public List<BadgeResponse> getMyBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(BadgeResponse::new)
                .toList();
    }

    public StreakResponse getMyStreak(Long userId) {
        return streakRecordRepository.findByUserId(userId)
                .map(StreakResponse::new)
                .orElseGet(StreakResponse::empty);
    }
}