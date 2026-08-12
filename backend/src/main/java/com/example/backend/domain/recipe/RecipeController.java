package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 지금은 로그인이 아직 없어서, 테스트하기 편하게 쿼리파라미터로 userId를 임시로 받음.
    // 예: GET /api/v1/recipes/recommend?userId=1
    @GetMapping("/recommend")
    public List<RecipeRecommendResponse> recommendRecipes(@RequestParam("userId") Long userId) {
        return recipeService.recommendRecipes(userId);
    }

    // 커뮤니티 글쓰기 화면의 카테고리 선택 드롭다운용
    // 예: GET /api/v1/recipes/categories
    @GetMapping("/categories")
    public List<RecipeCategoryResponse> getCategories() {
        return recipeService.getCategories();
    }

    // 예: GET /api/v1/recipes/1
    @GetMapping("/{recipeId}")
    public RecipeDetailResponse getRecipeDetail(@PathVariable Long recipeId) {
        return recipeService.getRecipeDetail(recipeId);
    }
}