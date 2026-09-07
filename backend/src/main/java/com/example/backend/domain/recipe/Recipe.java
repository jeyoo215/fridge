package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;

// ERD의 recipe 테이블 (레시피 마스터: 이름, 조리시간, 난이도 등 기본 정보)
@Entity
@Table(name = "recipe", uniqueConstraints = @UniqueConstraint(name = "uk_recipe_source_external", columnNames = {
        "source", "external_id" }))
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

    
    // 레시피 출처. null이면 관리자/공식 등록, "커뮤니티"면 커뮤니티 게시글이 승격되어 생성된 것.
    @Column(name = "source", length = 50)
    private String source;

    @CurrentTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CookingStep> cookingSteps = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeTool> recipeTools = new ArrayList<>();

    @Builder
    public Recipe(RecipeCategory category, String recipeName, Integer cookingTimeMinutes,
            String difficulty, String imageUrl, String rawIngredients,
            String source, String externalId,
            Double calorie, Double carbohydrate, Double protein,
            Double fat, Double sodium) {
        this.category = category;
        this.recipeName = recipeName;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.rawIngredients = rawIngredients;
        this.source = source;
        this.externalId = externalId;
        this.calorie = calorie;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.sodium = sodium;
    }

    // 연관관계 편의 메서드: 재료/조리단계/조리도구 추가 시 양방향 동기화
    public void addRecipeIngredient(RecipeIngredient recipeIngredient) {
        recipeIngredients.add(recipeIngredient);
        recipeIngredient.setRecipe(this);
    }

    public void addCookingStep(CookingStep cookingStep) {
        cookingSteps.add(cookingStep);
        cookingStep.setRecipe(this);
    }

    public void addRecipeTool(RecipeTool recipeTool) {
        recipeTools.add(recipeTool);
        recipeTool.setRecipe(this);
    }

    // 원본 재료 문자열 보존
    @Column(name = "raw_ingredients", columnDefinition = "TEXT")
    private String rawIngredients;

    // 외부 API 고유번호 (RCP_SEQ) — 중복 수집 방지
    @Column(name = "external_id", length = 50)
    private String externalId;

    // 영양성분
    @Column(name = "calorie")
    private Double calorie; // INFO_ENG (열량)

    @Column(name = "carbohydrate")
    private Double carbohydrate; // INFO_CAR (탄수화물)

    @Column(name = "protein")
    private Double protein; // INFO_PRO (단백질)

    @Column(name = "fat")
    private Double fat; // INFO_FAT (지방)

    @Column(name = "sodium")
    private Double sodium; // INFO_NA (나트륨)

}
