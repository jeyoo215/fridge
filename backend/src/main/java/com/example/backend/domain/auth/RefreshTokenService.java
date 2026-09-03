package com.example.backend.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveOrUpdate(Long userId, String refreshToken, long expirationMs) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder().userId(userId).token(refreshToken).expiresAt(expiresAt).build()
                        )
                );
    }
}