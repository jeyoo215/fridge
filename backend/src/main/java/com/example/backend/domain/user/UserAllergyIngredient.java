package com.example.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;

// ERD의 user_allergy_ingredient 테이블. 재료 마스터(ingredient)와 연결하지 않고,
// 사용자가 직접 텍스트로 입력한 알레르기/기피 재료만 저장한다 (요구사항: 입력 방식만 지원).
@Entity
@Table(name = "user_allergy_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAllergyIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ingredient_name", nullable = false, length = 50)
    private String ingredientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Type type;

    @Builder
    public UserAllergyIngredient(Long userId, String ingredientName, Type type) {
        this.userId = userId;
        this.ingredientName = ingredientName;
        this.type = type;
    }

    public enum Type {
        알레르기, 기피
    }
}
