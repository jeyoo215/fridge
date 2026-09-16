package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.IngredientCategoryResponse;
import com.example.backend.domain.ingredient.dto.IngredientCreateRequest;
import com.example.backend.domain.ingredient.dto.IngredientSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // 예: GET /api/v1/ingredients?keyword=양
    @GetMapping
    public List<IngredientSearchResponse> search(@RequestParam("keyword") String keyword) {
        return ingredientService.search(keyword);
    }

    // 카테고리 전체 목록 (새 재료 등록 화면의 드롭다운용)
    @GetMapping("/categories")
    public List<IngredientCategoryResponse> getCategories() {
        return ingredientService.getCategories();
    }

    // 재료 마스터에 없는 재료를 사용자가 직접 새로 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientSearchResponse createIngredient(@Valid @RequestBody IngredientCreateRequest request) {
        return ingredientService.createIngredient(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
