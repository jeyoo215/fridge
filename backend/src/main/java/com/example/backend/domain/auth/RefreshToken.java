package com.example.backend.domain.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 리프레시 토큰 저장 테이블. 유저 1명이 여러 세션(탭/기기/서버)을 동시에 가질 수 있도록
// user_id는 unique가 아니고, 세션을 식별하는 키는 token 자체(unique)다.
// 예전엔 user_id가 unique라서 탭 두 개, 혹은 로컬/배포 서버가 같은 유저로 동시에
// 로그인하면 서로의 refreshToken을 덮어써서 reissue가 계속 실패하는 문제가 있었다.
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // 이 세션(row) 자체를 새 토큰으로 회전시킴 — 다른 세션의 row는 전혀 건드리지 않음
    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}