package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeParsingService recipeParsingService;
    private final RecipeImportService recipeImportService;

    // 레시피 등록 (FR-24)
    // 재료 목록/조리순서 목록까지 요청 본문 한 번에 받아서 같이 저장
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createRecipe(@RequestBody RecipeCreateRequest request) {
        return recipeService.createRecipe(request);
    }

    // 레시피 상세조회 (FR-24)
    @GetMapping("/{recipeId}")
    public RecipeDetailResponse getRecipeDetail(@PathVariable Long recipeId) {
        return recipeService.getRecipeDetail(recipeId);
    }

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 지금은 로그인이 아직 없어서, 테스트하기 편하게 쿼리파라미터로 userId를 임시로 받음.
    // 예: GET /api/v1/recipes/recommend?userId=1
    @GetMapping("/recommend")
    public List<RecipeRecommendResponse> recommendRecipes(@RequestParam Long userId) {
        return recipeService.recommendRecipes(userId);
    }

    // 레시피 카테고리 목록 조회 (등록 화면 드롭다운용)
    @GetMapping("/categories")
    public List<RecipeCategoryResponse> getCategories() {
        return recipeService.getCategories();
    }

    // 레시피 재료 LLM 파싱 (임시 관리용 - limit건만 파싱)
    // 예: POST /api/v1/recipes/parse?limit=10
    @PostMapping("/parse")
    public int parseRecipes(@RequestParam(defaultValue = "10") int limit) {
        return recipeParsingService.parseRecipes(limit);
    }

    // 메서드
    @PostMapping("/import-steps")
    public int importSteps() {
        return recipeImportService.importCookingSteps();
    }
}