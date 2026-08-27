package com.example.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password; // 소셜 로그인만 쓰는 경우 NULL 허용

    // 회원가입 시점엔 임시값이 들어가고, 가입 후 마이페이지에서 직접 설정하는 값 (AuthService.signup 참고).
    // 다른 유저와 겹치면 안 되므로 unique 제약을 걸어둠 (실제 검증은 UserProfileService/AuthService에서 먼저 함).
    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    // 아이디(이메일) 찾기 본인 확인용. 소셜 로그인 계정은 이 값이 없을 수 있어 nullable로 둠
    // (카카오 프로필에서는 전화번호를 받지 않으므로).
    @Column(name = "phone", length = 20)
    private String phone;

    // 소셜 로그인 (FR-01)
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 100)
    private String providerId; // 소셜 로그인 공급자가 주는 고유 ID. 이메일 가입자는 null.

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String password, String nickname, String phone,
                AuthProvider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.createdAt = LocalDateTime.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 일반(LOCAL) 가입 계정에 카카오 로그인을 연결함 (계정 통합). 비밀번호는 그대로 둬서
    // 카카오 로그인과 기존 비밀번호 로그인 둘 다 계속 되게 함.
    public void linkKakao(String providerId) {
        this.provider = AuthProvider.KAKAO;
        this.providerId = providerId;
    }
}