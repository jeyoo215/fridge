package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ERD의 user_ingredient 테이블 (사용자가 실제로 냉장고에 갖고 있는 재료)
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

    // 소진/폐기 처리된 시점 (통계 집계 등에 사용). 아직 보유중이면 null.
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

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

    // --- 상태 변경 메서드 (소진/폐기 처리) ---
    public void consume() {
        this.status = Status.소진;
        this.resolvedAt = LocalDateTime.now();
    }

    public void discard() {
        this.status = Status.폐기;
        this.resolvedAt = LocalDateTime.now();
    }

    // 소진/폐기 처리를 실수로 눌렀을 때 되돌리기용
    public void restore() {
        this.status = Status.보유중;
        this.resolvedAt = null;
    }

    public void updateQuantityAndExpiration(BigDecimal quantity, LocalDate purchaseDate, LocalDate expirationDate) {
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
    }

    public enum Status {
        보유중, 소진, 폐기
    }
}
