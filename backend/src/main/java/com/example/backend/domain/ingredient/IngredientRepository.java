package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 재료 마스터 조회용 Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // 재료명에 검색어가 포함된 재료를 찾음 (등록 화면 자동완성용)
    List<Ingredient> findTop10ByIngredientNameContaining(String keyword);
}
