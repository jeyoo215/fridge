package com.example.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ERD의 user 테이블 (회원 기본 정보). 회원가입/로그인 로직은 별도 작업이라 엔티티만 우선 정의.
// UserIngredient 등 다른 엔티티의 user_id는 아직 이 엔티티에 대한 FK로 연결돼 있지 않고
// TODO로 남겨둔 상태이니, 나중에 회원 기능을 붙일 때 같이 정리하면 됨.
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

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }
}
