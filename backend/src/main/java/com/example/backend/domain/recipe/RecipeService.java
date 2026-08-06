package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    // 유통기한 가중치 기준 (FR-21)
    private static final int EXPIRY_WEIGHT_D1 = 3; // D-1 이하
    private static final int EXPIRY_WEIGHT_D3 = 2; // D-3 이하

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository; // 📌 새로 생성한 Repository 주입
    private final IngredientRepository ingredientRepository;
    private final UserIngredientRepository userIngredientRepository;

    // 레시피 등록 (FR-24)
    @Transactional
    public Long createRecipe(RecipeCreateRequest request) {
        RecipeCategory category = recipeCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리입니다. id=" + request.categoryId()));

        Recipe recipe = Recipe.builder()
                .category(category)
                .recipeName(request.recipeName())
                .cookingTimeMinutes(request.cookingTimeMinutes())
                .difficulty(request.difficulty())
                .imageUrl(request.imageUrl())
                .build();

        // 재료 목록 연결
        for (RecipeCreateRequest.IngredientItem item : request.ingredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.ingredientId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 재료입니다. id=" + item.ingredientId()));
            recipe.addRecipeIngredient(RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(item.quantity())
                    .unit(item.unit())
                    .build());
        }

        // 조리 순서 연결
        for (RecipeCreateRequest.StepItem item : request.steps()) {
            recipe.addCookingStep(CookingStep.builder()
                    .stepOrder(item.stepOrder())
                    .description(item.description())
                    .build());
        }

        // 조리도구 연결
        for (Long toolId : request.toolIds()) {
            recipe.addRecipeTool(RecipeTool.builder()
                    .toolId(toolId)
                    .build());
        }

        return recipeRepository.save(recipe).getRecipeId();
    }

    // 보유 재료 기반 레시피 추천 (FR-20 + FR-21 유통기한 가중치)
    public List<RecipeRecommendResponse> recommendRecipes(Long userId) {
        List<UserIngredient> myIngredients = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중);

        List<Long> ingredientIds = myIngredients.stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .toList();

        if (ingredientIds.isEmpty()) {
            return List.of();
        }

        // 재료 id -> 유통기한까지 남은 일수(dDay) 매핑 (가중치 계산용)
        Map<Long, Long> dDayByIngredientId = myIngredients.stream()
                .collect(Collectors.toMap(
                        userIngredient -> userIngredient.getIngredient().getIngredientId(),
                        userIngredient -> ChronoUnit.DAYS.between(LocalDate.now(), userIngredient.getExpirationDate()),
                        (existing, duplicate) -> existing
                ));

        List<RecipeRepository.RecipeMatchResult> matchResults =
                recipeRepository.findRecipesByMatchingIngredients(ingredientIds);

        Map<Long, Long> matchCountByRecipeId = matchResults.stream()
                .collect(Collectors.toMap(
                        RecipeRepository.RecipeMatchResult::getRecipeId,
                        RecipeRepository.RecipeMatchResult::getMatchCount
                ));

        List<Long> recipeIds = matchResults.stream()
                .map(RecipeRepository.RecipeMatchResult::getRecipeId)
                .toList();

        return recipeRepository.findAllById(recipeIds).stream()
                .map(recipe -> {
                    long matchCount = matchCountByRecipeId.get(recipe.getRecipeId());
                    int expiryPriorityScore = calculateExpiryPriorityScore(recipe, dDayByIngredientId);
                    return new RecipeRecommendResponse(recipe, matchCount, expiryPriorityScore);
                })
                .sorted(
                        Comparator
                                .comparingInt(RecipeRecommendResponse::getExpiryPriorityScore).reversed()
                                .thenComparing(Comparator.comparingLong(RecipeRecommendResponse::getMatchCount).reversed())
                )
                .toList();
    }

    // 레시피가 사용하는 재료 중, 보유 재료와 겹치는 것들의 유통기한 가중치 합산
    private int calculateExpiryPriorityScore(Recipe recipe, Map<Long, Long> dDayByIngredientId) {
        return recipe.getRecipeIngredients().stream()
                .mapToInt(recipeIngredient -> {
                    Long dDay = dDayByIngredientId.get(recipeIngredient.getIngredient().getIngredientId());
                    if (dDay == null) return 0;
                    if (dDay <= 1) return EXPIRY_WEIGHT_D1;
                    if (dDay <= 3) return EXPIRY_WEIGHT_D3;
                    return 0;
                })
                .sum();
    }

    // 레시피 상세 조회 (FR-24)
    public RecipeDetailResponse getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
        return new RecipeDetailResponse(recipe);
    }

    // 레시피 카테고리 목록 조회 (등록 화면 드롭다운용)
    public List<RecipeCategoryResponse> getCategories() {
        return recipeCategoryRepository.findAll().stream()
                .map(RecipeCategoryResponse::new)
                .toList();
    }
}
