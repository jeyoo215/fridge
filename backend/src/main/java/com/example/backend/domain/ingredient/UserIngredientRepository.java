package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserIngredientRepository extends JpaRepository<UserIngredient, Long> {

    // 아직 소진하지 않은(consumed=false) 재료를, 유통기한 임박한 순서로 조회
    List<UserIngredient> findByUserIdAndConsumedFalseOrderByExpirationDateAsc(Long userId);
}
