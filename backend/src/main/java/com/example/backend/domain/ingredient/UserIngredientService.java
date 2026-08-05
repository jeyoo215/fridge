package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
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
    private final IngredientRepository ingredientRepository;

    // 특정 유저의 보유 재료 목록 조회 (유통기한 임박한 순서)
    public List<UserIngredientResponse> getMyIngredients(Long userId) {
        return userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중)
                .stream()
                .map(UserIngredientResponse::new)
                .toList();
    }

    // 재료 수동 등록
    @Transactional
    public Long register(Long userId, UserIngredientRegisterRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다. ingredientId=" + request.ingredientId()));

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(userId)
                .ingredient(ingredient)
                .quantity(request.quantity())
                .unit(request.unit())
                .purchaseDate(request.purchaseDate())
                .expirationDate(request.expirationDate())
                .build();

        return userIngredientRepository.save(userIngredient).getUserIngredientId();
    }
}
