package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.UserAllergyIngredientRegisterRequest;
import com.example.backend.domain.user.dto.UserAllergyIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAllergyIngredientService {

    private final UserAllergyIngredientRepository userAllergyIngredientRepository;

    // 마이페이지에서 본인의 알레르기/기피 재료 목록 조회
    public List<UserAllergyIngredientResponse> getMyAllergyIngredients(Long userId) {
        return userAllergyIngredientRepository.findByUserId(userId)
                .stream()
                .map(UserAllergyIngredientResponse::new)
                .toList();
    }

    // 알레르기/기피 재료 직접 입력 등록
    @Transactional
    public Long register(Long userId, UserAllergyIngredientRegisterRequest request) {
        UserAllergyIngredient entity = UserAllergyIngredient.builder()
                .userId(userId)
                .ingredientName(request.ingredientName())
                .type(request.type())
                .build();

        return userAllergyIngredientRepository.save(entity).getId();
    }

    // 등록한 알레르기/기피 재료 삭제
    @Transactional
    public void delete(Long userId, Long id) {
        UserAllergyIngredient entity = userAllergyIngredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알레르기/기피 재료입니다. id=" + id));

        if (!entity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 등록한 항목만 삭제할 수 있습니다.");
        }
        userAllergyIngredientRepository.delete(entity);
    }
}
