package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// ERD의 recipe_ingredient 테이블 (레시피별 필요 재료 매핑)
@Entity
@Table(name = "recipe_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Builder
    public RecipeIngredient(Ingredient ingredient, BigDecimal quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Recipe에서만 호출 (같은 패키지 내부 전용, 외부 노출 안 함)
    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}