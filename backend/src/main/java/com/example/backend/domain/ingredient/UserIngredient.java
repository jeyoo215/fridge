package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ERD의 user_ingredient 테이블 (사용자가 실제로 냉장고에 갖고 있는 재료)
// 재료 정리는 "삭제" 하나로만 처리함 (소진/폐기 구분은 화면·API 단에서는 폐지).
// Status enum 자체는 챌린지 도메인(ChallengeService)이 "보유중" 값을 참조하고 있어서 그대로 유지함.
@Entity
@Table(name = "user_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_ingredient_id")
    private Long userIngredientId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 10)
    private String unit;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserIngredient(Long userId, Ingredient ingredient, BigDecimal quantity, String unit,
                           LocalDate purchaseDate, LocalDate expirationDate) {
        this.userId = userId;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
        this.status = Status.보유중;
        this.createdAt = LocalDateTime.now();
    }

    public void updateQuantityAndExpiration(BigDecimal quantity, LocalDate purchaseDate, LocalDate expirationDate) {
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
    }

    // 챌린지 도메인(ChallengeService)이 Status.보유중을 조회 조건으로 사용하므로 enum 자체는 유지함.
    // 소진/폐기 값은 더 이상 화면/API에서 만들지 않지만, 과거 데이터 호환을 위해 값만 남겨둠.
    public enum Status {
        보유중, 소진, 폐기
    }
}
