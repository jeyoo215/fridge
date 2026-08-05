package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
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

        return recipeRepository.save(recipe).getRecipeId();
    }

    // 레시피 상세조회 (FR-24)
    public RecipeDetailResponse getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
        return new RecipeDetailResponse(recipe);
    }

    // 보유 재료 기반 레시피 추천 (FR-20)
    // 1. 유저가 보유한 재료 id 목록을 뽑고
    // 2. 그 재료들과 겹치는 레시피를 매칭 개수 많은 순으로 조회
    public List<RecipeRecommendResponse> recommendRecipes(Long userId) {
        List<Long> ingredientIds = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중)
                .stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .toList();

        if (ingredientIds.isEmpty()) {
            return List.of();
        }

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
                .map(recipe -> new RecipeRecommendResponse(
                        recipe,
                        matchCountByRecipeId.get(recipe.getRecipeId())
                ))
                .sorted((a, b) -> Long.compare(b.getMatchCount(), a.getMatchCount()))
                .toList();
    }
}