package com.example.backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAllergyIngredientRepository extends JpaRepository<UserAllergyIngredient, Long> {

    // 마이페이지에서 본인의 알레르기/기피 재료 목록 조회
    List<UserAllergyIngredient> findByUserId(Long userId);
}
