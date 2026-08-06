package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 사용자별 냉장고 이름 (1인 1냉장고 기준, 기본값 "내 냉장고")
@Entity
@Table(name = "fridge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_id")
    private Long fridgeId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "fridge_name", nullable = false, length = 30)
    private String fridgeName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Fridge(Long userId, String fridgeName) {
        this.userId = userId;
        this.fridgeName = fridgeName;
        this.createdAt = LocalDateTime.now();
    }

    public void rename(String newName) {
        this.fridgeName = newName;
    }
}
