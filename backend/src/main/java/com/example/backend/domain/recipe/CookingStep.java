package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

// ERD의 cooking_step 테이블 (레시피별 조리 순서/설명). 커뮤니티 글의 조리순서 단계가 그대로 승격되면서
// 리치텍스트 본문과 이미지/동영상 첨부를 함께 옮겨받을 수 있다.
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

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", columnDefinition = "VARCHAR(10)")
    private MediaType mediaType;

    @Builder
    public CookingStep(Integer stepOrder, String description, String mediaUrl, MediaType mediaType) {
        this.stepOrder = stepOrder;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public enum MediaType {
        IMAGE, VIDEO
    }
}
