package com.example.backend.domain.recipe;

import com.example.backend.domain.user.CookingTool;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private CookingTool tool;

    @Builder
    public RecipeTool(CookingTool tool) {
        this.tool = tool;
    }

    // Recipe에서만 호출 (같은 패키지 내부 전용)
    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}