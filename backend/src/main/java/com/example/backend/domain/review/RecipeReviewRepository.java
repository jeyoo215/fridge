package com.example.backend.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeReviewRepository extends JpaRepository<RecipeReview, Long> {

    List<RecipeReview> findByRecipe_RecipeIdOrderByCreatedAtDesc(Long recipeId);

    @Query("SELECT AVG(r.rating) FROM RecipeReview r WHERE r.recipe.recipeId = :recipeId")
    Double findAverageRatingByRecipeId(@Param("recipeId") Long recipeId);
}