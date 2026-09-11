package com.example.backend.domain.user.dto;

import com.example.backend.domain.user.User;

public record UserProfileResponse(Long userId, String email, String nickname) {
    public UserProfileResponse(User user) {
        this(user.getUserId(), user.getEmail(), user.getNickname());
    }
}
