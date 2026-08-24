package com.example.backend.domain.badge;

import com.example.backend.domain.badge.dto.BadgeResponse;
import com.example.backend.domain.badge.dto.StreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/api/v1/users/me/badges")
    public List<BadgeResponse> getMyBadges(@AuthenticationPrincipal Long userId) {
        return badgeService.getMyBadges(userId);
    }

    @GetMapping("/api/v1/users/me/streak")
    public StreakResponse getMyStreak(@AuthenticationPrincipal Long userId) {
        return badgeService.getMyStreak(userId);
    }
}
