package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 재료 마스터 조회용 Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // 재료명에 검색어가 포함된 재료를 찾음 (등록 화면 자동완성용)
    List<Ingredient> findTop10ByIngredientNameContaining(String keyword);

    // 재료명 정확히 일치하는 재료 조회 (파싱 시 "있으면 가져오고 없으면 생성")
    Optional<Ingredient> findByIngredientName(String ingredientName);
}