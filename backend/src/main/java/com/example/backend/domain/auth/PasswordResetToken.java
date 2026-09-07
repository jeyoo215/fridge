package com.example.backend.domain.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 비밀번호 재설정 인증 코드 저장 테이블. 이메일당 1개만 유지(재요청 시 덮어씀).
// 흐름: 코드 발급 → verified 확인(코드 입력 화면) → 새 비밀번호 입력 칸 해금 → 최종 제출 시 used로 소모.
@Entity
@Table(name = "password_reset_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_reset_token_id")
    private Long passwordResetTokenId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Builder
    public PasswordResetToken(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.verified = false;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    // 재요청 시 새 코드로 덮어쓰고 인증 상태는 초기화
    public void update(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.verified = false;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public void markVerified() {
        this.verified = true;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
