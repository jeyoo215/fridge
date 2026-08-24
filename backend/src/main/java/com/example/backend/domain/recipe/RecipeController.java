package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipePageResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendPageResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/recommend")
    public RecipeRecommendPageResponse recommendRecipes(@AuthenticationPrincipal Long userId,
                                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        return recipeService.recommendRecipes(userId, page, size);
    }

    // 커뮤니티 글쓰기 화면의 카테고리 선택 드롭다운용
    @GetMapping("/categories")
    public List<RecipeCategoryResponse> getCategories() {
        return recipeService.getCategories();
    }

    @GetMapping("/{recipeId}")
    public RecipeDetailResponse getRecipeDetail(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getRecipeDetail(recipeId);
    }

    // 레시피 목록/검색 (페이징). 예: GET /api/v1/recipes?page=0&size=20&keyword=김치&ingredientIds=2,5
    @GetMapping
    public RecipePageResponse getList(@RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "20") int size,
                                    @RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "ingredientIds", required = false) List<Long> ingredientIds) {
        return recipeService.getList(keyword, ingredientIds, page, size);
    }
}
