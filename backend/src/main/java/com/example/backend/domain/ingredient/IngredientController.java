package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.IngredientSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final ingredientService ingredientService;

    // 예: GET /api/v1/ingredients?keyword=양
    @GetMapping
    public List<IngredientSearchResponse> search(@RequestParam("keyword") String keyword) {
        return ingredientService.search(keyword);
    }
}
