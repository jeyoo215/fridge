package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;

// ERD의 recipe 테이블 (레시피 마스터: 이름, 조리시간, 난이도 등 기본 정보)
@Entity
@Table(name = "recipe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private RecipeCategory category;

    @Column(name = "recipe_name", nullable = false, length = 200)
    private String recipeName;

    @Column(name = "cooking_time_minutes")
    private Integer cookingTimeMinutes;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CurrentTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CookingStep> cookingSteps = new ArrayList<>();

    @Builder
    public Recipe(RecipeCategory category, String recipeName, Integer cookingTimeMinutes,
                  String difficulty, String imageUrl) {
        this.category = category;
        this.recipeName = recipeName;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
    }

    // 연관관계 편의 메서드: 재료/조리단계 추가 시 양방향 동기화
    public void addRecipeIngredient(RecipeIngredient recipeIngredient) {
        recipeIngredients.add(recipeIngredient);
        recipeIngredient.setRecipe(this);
    }

    public void addCookingStep(CookingStep cookingStep) {
        cookingSteps.add(cookingStep);
        cookingStep.setRecipe(this);
    }
}