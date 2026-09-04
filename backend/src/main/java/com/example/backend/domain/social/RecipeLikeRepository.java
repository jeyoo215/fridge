package com.example.backend.domain.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeLikeRepository extends JpaRepository<RecipeLike, Long> {
    Optional<RecipeLike> findByRecipe_RecipeIdAndUserId(Long recipeId, Long userId);
    long countByRecipe_RecipeId(Long recipeId);
}
