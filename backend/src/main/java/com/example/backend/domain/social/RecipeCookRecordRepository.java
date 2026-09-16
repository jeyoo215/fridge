package com.example.backend.domain.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeCookRecordRepository extends JpaRepository<RecipeCookRecord, Long> {
    Optional<RecipeCookRecord> findByRecipe_RecipeIdAndUserId(Long recipeId, Long userId);
    long countByRecipe_RecipeId(Long recipeId);

    // 마이페이지 "내가 만들어본 레시피" 목록용, 최신순
    List<RecipeCookRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
