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
    // 보유 재료(조미료 제외)와 겹치는 비조미료 재료 개수 (레시피별)
    @Query("""
        SELECT ri.recipe.recipeId AS recipeId, COUNT(ri) AS matchCount
        FROM RecipeIngredient ri
        WHERE ri.ingredient.ingredientId IN :ingredientIds AND ri.ingredient.isSeasoning = false
        GROUP BY ri.recipe.recipeId
        """)
    List<RecipeMatchResult> findRecipesByMatchingNonSeasoningIngredients(@Param("ingredientIds") List<Long> ingredientIds);

    // 레시피별로 필요한 비조미료 재료 총 개수 (완전 매칭 여부 판단 기준값)
    @Query("""
        SELECT ri.recipe.recipeId AS recipeId, COUNT(ri) AS matchCount
        FROM RecipeIngredient ri
        WHERE ri.recipe.recipeId IN :recipeIds AND ri.ingredient.isSeasoning = false
        GROUP BY ri.recipe.recipeId
        """)
    List<RecipeMatchResult> findNonSeasoningIngredientCountByRecipeIdIn(@Param("recipeIds") List<Long> recipeIds);

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

    // 추천 랭킹 계산용: 후보 레시피들의 재료를 한 번에 fetch join (N+1 방지)
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.recipeIngredients ri LEFT JOIN FETCH ri.ingredient WHERE r.recipeId IN :recipeIds")
    List<Recipe> findAllWithIngredientsByRecipeIdIn(@Param("recipeIds") List<Long> recipeIds);

    // 추천 랭킹 계산용: 후보 레시피들이 요구하는 조리도구 id를 한 번에 조회 (N+1 방지)
    // recipeTools도 LEFT JOIN FETCH 하면 recipeIngredients랑 같이 못 묶어서(MultipleBagFetchException) 별도 쿼리로 분리
    @Query("SELECT rt.recipe.recipeId AS recipeId, rt.tool.toolId AS toolId FROM RecipeTool rt WHERE rt.recipe.recipeId IN :recipeIds")
    List<RecipeToolIdPair> findToolIdPairsByRecipeIdIn(@Param("recipeIds") List<Long> recipeIds);

    interface RecipeToolIdPair {
        Long getRecipeId();
        Long getToolId();
    }

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