package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.IngredientCategoryResponse;
import com.example.backend.domain.ingredient.dto.IngredientCreateRequest;
import com.example.backend.domain.ingredient.dto.IngredientSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryRepository ingredientCategoryRepository;

    // 재료 이름으로 검색 (등록 화면 자동완성)
    public List<IngredientSearchResponse> search(String keyword) {
        return ingredientRepository.findTop10ByIngredientNameContaining(keyword)
                .stream()
                .map(IngredientSearchResponse::new)
                .toList();
    }

    // 카테고리 전체 목록 (새 재료 등록 화면의 카테고리 선택용)
    public List<IngredientCategoryResponse> getCategories() {
        return ingredientCategoryRepository.findAll().stream()
                .map(IngredientCategoryResponse::new)
                .toList();
    }

    // 재료 마스터에 없는 재료를 사용자가 직접 새로 등록
    @Transactional
    public IngredientSearchResponse createIngredient(IngredientCreateRequest request) {
        IngredientCategory category = ingredientCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. categoryId=" + request.categoryId()));

        Ingredient.StorageMethod storageMethod = null;
        if (request.storageMethod() != null && !request.storageMethod().isBlank()) {
            try {
                storageMethod = Ingredient.StorageMethod.valueOf(request.storageMethod());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("보관법은 냉장/냉동/실온 중 하나여야 합니다.");
            }
        }

        Ingredient ingredient = Ingredient.builder()
                .category(category)
                .ingredientName(request.ingredientName().trim())
                .storageMethod(storageMethod)
                .build();

        return new IngredientSearchResponse(ingredientRepository.save(ingredient));
    }
}
