package com.example.backend.domain.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 이미 수집한 레시피인지 확인 (source + external_id 조합으로 중복 방지)
    boolean existsBySourceAndExternalId(String source, String externalId);

    // 보유 재료 id 목록(ingredientIds)과 매칭되는 재료 개수가 많은 순으로 레시피 id 조회
    // (레시피가 필요로 하는 재료 중, 사용자가 가진 재료가 몇 개 겹치는지를 매칭 점수로 사용 -> FR-20)
    @Query("""
            SELECT ri.recipe.recipeId AS recipeId, COUNT(ri) AS matchCount
            FROM RecipeIngredient ri
            WHERE ri.ingredient.ingredientId IN :ingredientIds
            GROUP BY ri.recipe.recipeId
            ORDER BY matchCount DESC
            """)
    List<RecipeMatchResult> findRecipesByMatchingIngredients(@Param("ingredientIds") List<Long> ingredientIds);

    interface RecipeMatchResult {
        Long getRecipeId();

        Long getMatchCount();
    }

    // (파싱 대상 조회 — 재료 연결 없는 것)
    @Query(value = """
            SELECT r FROM Recipe r
            WHERE r.rawIngredients IS NOT NULL
            AND r.recipeIngredients IS EMPTY
            """)
    List<Recipe> findRecipesToParse(org.springframework.data.domain.Pageable pageable);


    Optional<Recipe> findBySourceAndExternalId(String source, String externalId);
}