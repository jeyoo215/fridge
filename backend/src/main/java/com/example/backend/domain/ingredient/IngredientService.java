package com.example.backend.domain.ingredient;

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

    // 재료 이름으로 검색 (등록 화면 자동완성)
    public List<IngredientSearchResponse> search(String keyword) {
        return ingredientRepository.findTop10ByIngredientNameContaining(keyword)
                .stream()
                .map(IngredientSearchResponse::new)
                .toList();
    }
}
