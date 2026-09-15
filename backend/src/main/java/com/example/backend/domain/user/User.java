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
    private String password;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    // 관리자 권한 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "varchar(20) default 'USER'")
    private Role role;

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
        this.role = Role.USER;
        this.createdAt = LocalDateTime.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void linkKakao(String providerId) {
        this.provider = AuthProvider.KAKAO;
        this.providerId = providerId;
    }
}