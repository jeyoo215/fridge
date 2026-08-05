package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserIngredientService {

    private final UserIngredientRepository userIngredientRepository;

    // 특정 유저의 보유 재료 목록 조회 (유통기한 임박한 순서)
    public List<UserIngredientResponse> getMyIngredients(Long userId) {
        return userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중)
                .stream()
                .map(UserIngredientResponse::new)
                .toList();
    }
}
