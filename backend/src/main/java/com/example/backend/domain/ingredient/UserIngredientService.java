package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import com.example.backend.domain.ingredient.dto.UserIngredientUpdateRequest;
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

    // 재료 수정 (수량/구매일/소비기한 변경)
    @Transactional
    public void update(Long userId, Long userIngredientId, UserIngredientUpdateRequest request) {
        UserIngredient userIngredient = findOwnedUserIngredient(userId, userIngredientId);
        userIngredient.updateQuantityAndExpiration(request.quantity(), request.purchaseDate(), request.expirationDate());
    }

    // 재료 소진 처리 ("요리에 다 썼어요")
    @Transactional
    public void consume(Long userId, Long userIngredientId) {
        UserIngredient userIngredient = findOwnedUserIngredient(userId, userIngredientId);
        userIngredient.consume();
    }

    // 재료 폐기 처리 ("상해서 버렸어요")
    @Transactional
    public void discard(Long userId, Long userIngredientId) {
        UserIngredient userIngredient = findOwnedUserIngredient(userId, userIngredientId);
        userIngredient.discard();
    }

    // 소진/폐기 처리를 실수로 눌렀을 때 되돌리기
    @Transactional
    public void restore(Long userId, Long userIngredientId) {
        UserIngredient userIngredient = findOwnedUserIngredient(userId, userIngredientId);
        userIngredient.restore();
    }

    // 재료 목록에서 완전히 삭제 (잘못 등록한 경우 등)
    @Transactional
    public void delete(Long userId, Long userIngredientId) {
        UserIngredient userIngredient = findOwnedUserIngredient(userId, userIngredientId);
        userIngredientRepository.delete(userIngredient);
    }

    // 본인 소유의 재료가 맞는지 확인 후 반환 (다른 사람 재료를 못 건드리게 방지)
    private UserIngredient findOwnedUserIngredient(Long userId, Long userIngredientId) {
        UserIngredient userIngredient = userIngredientRepository.findById(userIngredientId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다. id=" + userIngredientId));

        if (!userIngredient.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 재료만 수정/삭제할 수 있습니다.");
        }
        return userIngredient;
    }
}
