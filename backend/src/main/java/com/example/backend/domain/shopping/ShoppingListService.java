package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.shopping.dto.ShoppingListItemResponse;
import com.example.backend.domain.shopping.dto.ShoppingListResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;

    // 특정 레시피 기준으로, 보유하지 않은(부족한) 재료 목록 생성 (FR-30)
    public ShoppingListResponse getShoppingList(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));

        // 내가 보유중인 재료 id 목록
        Set<Long> ownedIngredientIds = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중)
                .stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .collect(Collectors.toSet());

        // 레시피 필요 재료 중, 보유하지 않은 것만 부족 재료로 추림
        List<ShoppingListItemResponse> missingIngredients = recipe.getRecipeIngredients().stream()
                .filter(recipeIngredient -> !ownedIngredientIds.contains(recipeIngredient.getIngredient().getIngredientId()))
                .map(ShoppingListItemResponse::new)
                .toList();

        return new ShoppingListResponse(recipe.getRecipeId(), recipe.getRecipeName(), missingIngredients);
    }
}