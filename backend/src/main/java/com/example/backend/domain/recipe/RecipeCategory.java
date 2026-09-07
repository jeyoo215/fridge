package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

// ERD의 recipe_category 테이블 (레시피 분류: "한식", "양식" 같은 카테고리 정보)
@Entity
@Table(name = "recipe_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Builder
    public RecipeCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}