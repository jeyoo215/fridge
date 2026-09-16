package com.example.backend.domain.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 이미 수집한 레시피인지 확인 (source + external_id 조합으로 중복 방지)
    boolean existsBySourceAndExternalId(String source, String externalId);

    // 전체 목록 (검색조건 없을 때)
    @Query("SELECT r.recipeId FROM Recipe r ORDER BY r.recipeId")
    Page<Long> findAllRecipeIds(Pageable pageable);

    // 이름 검색만. 띄어쓰기 차이로 "감자 주스"가 "감자주스"를 못 찾는 문제를 막기 위해,
    // 저장된 이름과 검색어 둘 다 공백을 지우고 비교한다(검색어 쪽 공백 제거는 RecipeService에서 미리 해둠).
    @Query("SELECT r.recipeId FROM Recipe r WHERE REPLACE(r.recipeName, ' ', '') LIKE CONCAT('%', :keyword, '%') ORDER BY r.recipeId")
    Page<Long> findRecipeIdsByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    // 재료 필터만 (선택한 재료 중 하나라도 포함된 레시피, OR 매칭)
    @Query("""
        SELECT DISTINCT ri.recipe.recipeId FROM RecipeIngredient ri
        WHERE ri.ingredient.ingredientId IN :ingredientIds
        ORDER BY ri.recipe.recipeId
        """)
    Page<Long> findRecipeIdsByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds, Pageable pageable);

    // 이름 + 재료 필터 동시
    @Query("""
        SELECT DISTINCT ri.recipe.recipeId FROM RecipeIngredient ri
        WHERE REPLACE(ri.recipe.recipeName, ' ', '') LIKE CONCAT('%', :keyword, '%')
          AND ri.ingredient.ingredientId IN :ingredientIds
        ORDER BY ri.recipe.recipeId
        """)
    Page<Long> findRecipeIdsByNameAndIngredientIds(@Param("keyword") String keyword,
                                                    @Param("ingredientIds") List<Long> ingredientIds,
                                                    Pageable pageable);

    // (파싱 대상 조회 — 재료 연결 없는 것)
    @Query(value = """
            SELECT r FROM Recipe r
            WHERE r.rawIngredients IS NOT NULL
            AND r.recipeIngredients IS EMPTY
            """)
    List<Recipe> findRecipesToParse(org.springframework.data.domain.Pageable pageable);


    Optional<Recipe> findBySourceAndExternalId(String source, String externalId);
}