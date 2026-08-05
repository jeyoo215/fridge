package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserIngredientRepository extends JpaRepository<UserIngredient, Long> {

    // 특정 유저가 보유한 재료를, 유통기한 임박한 순서로 조회 (유통기한 우선순위 추천에 그대로 활용 가능)
    List<UserIngredient> findByUserIdAndStatusOrderByExpirationDateAsc(Long userId, UserIngredient.Status status);
}
