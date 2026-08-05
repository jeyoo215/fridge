package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

// 재료 마스터 조회용 Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}