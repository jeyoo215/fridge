package com.example.backend.domain.recipe;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// 레시피 카테고리 조회용 Repository
public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, Long> {

    // 카테고리 이름으로 조회 (수집 시 "있으면 가져오고 없으면 생성"에 사용)
     Optional<RecipeCategory> findByCategoryName(String categoryName);
}



 