package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

// ERD의 recipe_tool 테이블 (레시피별 필요 조리도구 매핑, FR-22)
@Entity
@Table(name = "recipe_tool")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // TODO: 페어1이 CookingTool 엔티티 만들면 @ManyToOne으로 교체
    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Builder
    public RecipeTool(Long toolId) {
        this.toolId = toolId;
    }

    // Recipe에서만 호출 (같은 패키지 내부 전용)
    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
