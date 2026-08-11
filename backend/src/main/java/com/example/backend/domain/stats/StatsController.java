package com.example.backend.domain.stats;

import com.example.backend.domain.stats.dto.MonthlyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: GET /api/v1/users/me/stats/monthly?userId=1&yearMonth=2026-08
    @GetMapping("/api/v1/users/me/stats/monthly")
    public MonthlyStatsResponse getMonthlyStats(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "yearMonth", required = false) String yearMonth) {
        return statsService.getMonthlyStats(userId, yearMonth);
    }
}
