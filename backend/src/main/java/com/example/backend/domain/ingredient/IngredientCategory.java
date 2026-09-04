package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

// ERD의 ingredient_category 테이블과 매칭되는 클래스
@Entity
@Table(name = "ingredient_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요해서 열어두되, 외부에서 함부로 못 만들게 protected로
public class IngredientCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 30)
    private String categoryName;

    @Builder
    public IngredientCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
