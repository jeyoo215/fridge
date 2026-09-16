package com.example.backend.domain.community.dto;

import lombok.Getter;

@Getter
public class CommunityNotificationUnreadCountResponse {

    private final long unreadCount;

    public CommunityNotificationUnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
