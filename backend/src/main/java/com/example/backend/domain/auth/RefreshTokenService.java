package com.example.backend.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인할 때마다 "새 세션"을 하나 추가하는 것 — 기존 세션(다른 탭/기기)은 건드리지 않음
    @Transactional
    public void save(Long userId, String refreshToken, long expirationMs) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
        refreshTokenRepository.save(
                RefreshToken.builder().userId(userId).token(refreshToken).expiresAt(expiresAt).build()
        );
    }

    // 만료된 세션 row들을 매일 새벽에 청소 (안 하면 테이블에 계속 쌓임)
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("[RefreshToken] 만료된 리프레시 토큰 정리 완료");
    }
}