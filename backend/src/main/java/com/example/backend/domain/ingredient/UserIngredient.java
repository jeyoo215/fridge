package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// schema.sql의 user_ingredient 테이블과 매칭 (팀 공식 스키마 기준)
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

    @Column(length = 20)
    private String unit;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // schema.sql 기준: status(enum) 대신 is_consumed(boolean) 사용
    @Column(name = "is_consumed", nullable = false)
    private boolean consumed;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserIngredient(Long userId, Ingredient ingredient, BigDecimal quantity, String unit,
                           LocalDate purchaseDate, LocalDate expirationDate) {
        this.userId = userId;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
        this.consumed = false;
    }

    // --- 상태 변경 메서드 ---
    public void markConsumed() {
        this.consumed = true;
    }

    public void updateQuantityAndExpiration(BigDecimal quantity, LocalDate expirationDate) {
        this.quantity = quantity;
        this.expirationDate = expirationDate;
    }
}
