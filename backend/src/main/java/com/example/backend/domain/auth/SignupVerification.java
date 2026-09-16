package com.example.backend.domain.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 회원가입 이메일 인증 코드 저장 테이블. 이메일당 1개만 유지(재요청 시 덮어씀).
// PasswordResetToken과 구조는 같지만, "인증했는지" 여부(verified)를 signup()에서 반드시 확인해야 해서
// used(1회성 소모) 대신 verified(인증 완료 상태) 플래그를 쓴다.
@Entity
@Table(name = "signup_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signup_verification_id")
    private Long signupVerificationId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public SignupVerification(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    // 재요청 시 새 코드로 덮어쓰고 인증 상태는 초기화
    public void update(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public void markVerified() {
        this.verified = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
