package com.example.backend.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    List<RefreshToken> findAllByUserId(Long userId); // 전체 기기 로그아웃 등에 필요시 사용
    void deleteByExpiresAtBefore(LocalDateTime cutoff); // 만료된 세션 정리용
}