package com.example.backend.domain.badge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private StreakRecordRepository streakRecordRepository;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    @DisplayName("첫 챌린지 성공 시 스트릭 레코드가 새로 생성되고 1로 증가한다")
    void onChallengeSuccess_첫성공이면_스트릭레코드생성() {
        when(streakRecordRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(streakRecordRepository.save(any(StreakRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(badgeRepository.findByConditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT))
                .thenReturn(List.of());
        when(badgeRepository.findByConditionType(Badge.ConditionType.STREAK_COUNT))
                .thenReturn(List.of());

        badgeService.onChallengeSuccess(1L);

        verify(streakRecordRepository).save(argThat(streak ->
                streak.getCurrentStreak() == 1 && streak.getTotalSuccessCount() == 1
        ));
    }

    @Test
    @DisplayName("누적 성공 횟수 조건을 만족하면 뱃지가 지급된다")
    void onChallengeSuccess_조건충족하면_뱃지지급() {
        StreakRecord existing = StreakRecord.builder().userId(1L).build();

        Badge firstStepBadge = Badge.builder()
                .badgeName("첫 걸음").description("첫 챌린지 성공")
                .conditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT).conditionValue(1)
                .build();
        setBadgeId(firstStepBadge, 100L);

        when(streakRecordRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(badgeRepository.findByConditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT))
                .thenReturn(List.of(firstStepBadge));
        when(badgeRepository.findByConditionType(Badge.ConditionType.STREAK_COUNT))
                .thenReturn(List.of());
        when(userBadgeRepository.existsByUserIdAndBadge_BadgeId(1L, 100L)).thenReturn(false);

        badgeService.onChallengeSuccess(1L);

        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    @DisplayName("이미 획득한 뱃지는 중복 지급되지 않는다")
    void onChallengeSuccess_이미획득한뱃지는_중복지급안됨() {
        StreakRecord existing = StreakRecord.builder().userId(1L).build();

        Badge firstStepBadge = Badge.builder()
                .badgeName("첫 걸음").description("첫 챌린지 성공")
                .conditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT).conditionValue(1)
                .build();
        setBadgeId(firstStepBadge, 100L);

        when(streakRecordRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(badgeRepository.findByConditionType(Badge.ConditionType.CHALLENGE_SUCCESS_COUNT))
                .thenReturn(List.of(firstStepBadge));
        when(badgeRepository.findByConditionType(Badge.ConditionType.STREAK_COUNT))
                .thenReturn(List.of());
        when(userBadgeRepository.existsByUserIdAndBadge_BadgeId(1L, 100L)).thenReturn(true);

        badgeService.onChallengeSuccess(1L);

        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    @Test
    @DisplayName("챌린지 실패 시 연속 스트릭만 초기화되고 누적 성공 횟수는 유지된다")
    void onChallengeFailed_스트릭초기화() {
        StreakRecord existing = StreakRecord.builder().userId(1L).build();
        existing.increaseOnSuccess();
        existing.increaseOnSuccess();
        assertThat(existing.getCurrentStreak()).isEqualTo(2);

        when(streakRecordRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        badgeService.onChallengeFailed(1L);

        assertThat(existing.getCurrentStreak()).isEqualTo(0);
        assertThat(existing.getTotalSuccessCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("스트릭 기록이 없는 유저는 기본값(0)을 반환한다")
    void getMyStreak_기록없으면_기본값() {
        when(streakRecordRepository.findByUserId(99L)).thenReturn(Optional.empty());

        var response = badgeService.getMyStreak(99L);

        assertThat(response.getCurrentStreak()).isEqualTo(0);
        assertThat(response.getLongestStreak()).isEqualTo(0);
        assertThat(response.getTotalSuccessCount()).isEqualTo(0);
    }

    private void setBadgeId(Badge badge, Long id) {
        ReflectionTestUtils.setField(badge, "badgeId", id);
    }
}