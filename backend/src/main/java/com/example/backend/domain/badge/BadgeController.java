package com.example.backend.domain.badge;

import com.example.backend.domain.badge.dto.BadgeResponse;
import com.example.backend.domain.badge.dto.StreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @GetMapping("/api/v1/users/me/badges")
    public List<BadgeResponse> getMyBadges(@RequestParam("userId") Long userId) {
        return badgeService.getMyBadges(userId);
    }

    @GetMapping("/api/v1/users/me/streak")
    public StreakResponse getMyStreak(@RequestParam("userId") Long userId) {
        return badgeService.getMyStreak(userId);
    }
}