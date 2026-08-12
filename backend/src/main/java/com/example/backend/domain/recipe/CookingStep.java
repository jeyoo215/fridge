package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

// ERD의 cooking_step 테이블 (레시피별 조리 순서/설명)
@Entity
@Table(name = "cooking_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CookingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    public CookingStep(Integer stepOrder, String description, String imageUrl) {
        this.stepOrder = stepOrder;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

}