package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.ComboRecommendResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComboRecommendationService {

    private final ComboRecommendationRepository comboRecommendationRepository;
    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;

    // 조합 추천 조회 - 순위(combo_score)는 Python 배치가 미리 계산해둔 값을 그대로 씀.
    // "있는 재료만 활용" 필터는 배치 결과(fully_matched)를 안 믿고, 지금 이 순간의
    // 보유 재료로 즉석 재판정 -> 재료를 방금 등록해도 다음 배치를 안 기다리고 바로 반영됨.
    public List<ComboRecommendResponse> getComboRecommendations(Long userId, boolean onlyOwnedIngredients) {
        List<ComboRecommendation> all = comboRecommendationRepository.findByUserIdOrderByComboScoreDesc(userId);

        if (all.isEmpty() || !onlyOwnedIngredients) {
            return all.stream().map(ComboRecommendResponse::new).toList();
        }

        List<Long> recipeIds = all.stream().map(c -> c.getRecipe().getRecipeId()).toList();

        Set<Long> ownedIngredientIds = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중).stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .collect(Collectors.toSet());

        Map<Long, Set<Long>> essentialIdsByRecipe = recipeRepository
                .findNonSeasoningIngredientIdsByRecipeIdIn(recipeIds).stream()
                .collect(Collectors.groupingBy(
                        RecipeRepository.RecipeEssentialIdPair::getRecipeId,
                        Collectors.mapping(RecipeRepository.RecipeEssentialIdPair::getIngredientId, Collectors.toSet())
                ));

        return all.stream()
                .filter(c -> ownedIngredientIds.containsAll(
                        essentialIdsByRecipe.getOrDefault(c.getRecipe().getRecipeId(), Set.of())))
                .map(ComboRecommendResponse::new)
                .toList();
    }
}