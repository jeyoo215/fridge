package com.example.backend.domain.badge.dto;

import com.example.backend.domain.badge.UserBadge;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class BadgeResponse {
    private final Long badgeId;
    private final String badgeName;
    private final String description;
    private final LocalDateTime earnedAt;

    public BadgeResponse(UserBadge entity) {
        this.badgeId = entity.getBadge().getBadgeId();
        this.badgeName = entity.getBadge().getBadgeName();
        this.description = entity.getBadge().getDescription();
        this.earnedAt = entity.getEarnedAt();
    }
}