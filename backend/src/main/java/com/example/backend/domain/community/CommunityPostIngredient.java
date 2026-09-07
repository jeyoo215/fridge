package com.example.backend.domain.community;

import com.example.backend.domain.ingredient.Ingredient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// 커뮤니티 레시피 글의 재료 목록 (recipe.RecipeIngredient와 동일 구조).
// 정식 레시피로 승격될 때 이 목록이 RecipeIngredient로 그대로 옮겨간다.
@Entity
@Table(name = "community_post_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Builder
    public CommunityPostIngredient(Ingredient ingredient, BigDecimal quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    // CommunityPost에서만 호출 (같은 패키지 내부 전용)
    void setPost(CommunityPost post) {
        this.post = post;
    }
}
