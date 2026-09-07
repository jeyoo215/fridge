package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 관리자 전용 레시피 등록. SecurityConfig에서 /api/v1/admin/** 는 hasRole("ADMIN")으로 보호됨.
@RestController
@RequestMapping("/api/v1/admin/recipes")
@RequiredArgsConstructor
public class AdminRecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createRecipe(@Valid @RequestBody RecipeCreateRequest request) {
        return Map.of("recipeId", recipeService.createRecipe(request));
    }
}