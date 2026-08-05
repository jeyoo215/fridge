package com.example.backend.domain.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

// 레시피 카테고리 조회용 Repository
public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, Long> {
}