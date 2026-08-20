package com.example.backend.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeReviewRepository extends JpaRepository<RecipeReview, Long> {

    List<RecipeReview> findByRecipe_RecipeIdOrderByCreatedAtDesc(Long recipeId);

    // 마이페이지 "내가 평가한 레시피" 목록용, 최신순
    List<RecipeReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT AVG(r.rating) FROM RecipeReview r WHERE r.recipe.recipeId = :recipeId")
    Double findAverageRatingByRecipeId(@Param("recipeId") Long recipeId);
}