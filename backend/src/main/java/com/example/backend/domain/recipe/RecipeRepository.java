package com.example.backend.domain.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

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

    // 이름 검색만
    @Query("SELECT r.recipeId FROM Recipe r WHERE r.recipeName LIKE CONCAT('%', :keyword, '%') ORDER BY r.recipeId")
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
        WHERE ri.recipe.recipeName LIKE CONCAT('%', :keyword, '%')
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
}