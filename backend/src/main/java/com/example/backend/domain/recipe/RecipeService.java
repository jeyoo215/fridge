package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
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
    private final UserIngredientRepository userIngredientRepository;

    // 보유 재료 기반 레시피 추천 (FR-20)
    // 1. 유저가 보유한 재료 id 목록을 뽑고
    // 2. 그 재료들과 겹치는 레시피를 매칭 개수 많은 순으로 조회
    public List<RecipeRecommendResponse> recommendRecipes(Long userId) {
        List<Long> ingredientIds = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중).stream()
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