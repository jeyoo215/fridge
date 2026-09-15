package com.example.backend.domain.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeScrapRepository extends JpaRepository<RecipeScrap, Long> {
    Optional<RecipeScrap> findByRecipe_RecipeIdAndUserId(Long recipeId, Long userId);
    long countByRecipe_RecipeId(Long recipeId);

    // 마이페이지 "내가 스크랩한 레시피 모아보기"용, 최신순
    List<RecipeScrap> findByUserIdOrderByCreatedAtDesc(Long userId);
}
