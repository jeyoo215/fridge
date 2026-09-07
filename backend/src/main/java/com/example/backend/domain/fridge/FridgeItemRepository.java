package com.example.backend.domain.fridge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {

    // 특정 유저의 냉장고에 배치된 재료 전부 조회
    // (user_ingredient를 통해 userId로 필터)
    @Query("""
            SELECT fi FROM FridgeItem fi
            JOIN FETCH fi.userIngredient ui
            JOIN FETCH ui.ingredient
            WHERE ui.userId = :userId
            """)
    List<FridgeItem> findAllByUserId(@Param("userId") Long userId);

    // 중복 배치 방지용
    boolean existsByUserIngredient_UserIngredientId(Long userIngredientId);
}