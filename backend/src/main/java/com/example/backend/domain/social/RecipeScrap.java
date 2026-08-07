package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 레시피 스크랩 (나중에 다시 보려고 저장해두는 것, 사용자 1명이 레시피 1개당 최대 1번)
@Entity
@Table(name = "recipe_scrap", uniqueConstraints = @UniqueConstraint(columnNames = {"recipe_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long scrapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RecipeScrap(Recipe recipe, Long userId) {
        this.recipe = recipe;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
