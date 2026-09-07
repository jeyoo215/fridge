package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

// ERD의 ingredient 테이블 (재료 마스터: "양파", "당근" 같은 재료 자체 정보)
@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private IngredientCategory category;

    @Column(name = "ingredient_name", nullable = false, length = 50)
    private String ingredientName;

    @Column(name = "default_shelf_life_days")
    private Integer defaultShelfLifeDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_method")
    private StorageMethod storageMethod;

    @Column(name = "is_seasoning")
    private boolean isSeasoning;

    @Builder
    public Ingredient(IngredientCategory category, String ingredientName,
                       Integer defaultShelfLifeDays, StorageMethod storageMethod, boolean isSeasoning) {
        this.category = category;
        this.ingredientName = ingredientName;
        this.defaultShelfLifeDays = defaultShelfLifeDays;
        this.storageMethod = storageMethod;
        this.isSeasoning = isSeasoning;
    }

    public enum StorageMethod {
        냉장, 냉동, 실온
    }
}
