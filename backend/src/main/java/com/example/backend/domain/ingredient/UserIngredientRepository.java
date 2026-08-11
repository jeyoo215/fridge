package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserIngredientRepository extends JpaRepository<UserIngredient, Long> {

    // 특정 유저가 보유한 재료를, 유통기한 임박한 순서로 조회
    List<UserIngredient> findByUserIdAndStatusOrderByExpirationDateAsc(Long userId, UserIngredient.Status status);

    // 통계용: 특정 기간(주로 한 달) 안에 소진/폐기 처리된 재료 전부 (월간 통계 계산에 사용)
    List<UserIngredient> findByUserIdAndResolvedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
