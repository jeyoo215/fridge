package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeIngredient;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.shopping.dto.ManualShoppingItemRequest;
import com.example.backend.domain.shopping.dto.MyShoppingListResponse;
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
    private final ShoppingListRepository shoppingListRepository;
    private final IngredientRepository ingredientRepository;

    // 특정 레시피 기준 부족 재료 미리보기 (저장 안 함, 기존 기능 그대로 유지) (FR-30)
    public ShoppingListResponse getShoppingList(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));

        List<ShoppingListItemResponse> missingIngredients = findMissingRecipeIngredients(userId, recipe).stream()
                .map(ShoppingListItemResponse::new)
                .toList();

        return new ShoppingListResponse(recipe.getRecipeId(), recipe.getRecipeName(), missingIngredients);
    }

    // 레시피의 부족한 재료를 내 장보기 리스트에 담기 (이미 담긴 재료는 중복으로 안 담음)
    @Transactional
    public void addMissingIngredientsToMyList(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));

        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseGet(() -> shoppingListRepository.save(ShoppingList.builder().userId(userId).build()));

        for (RecipeIngredient recipeIngredient : findMissingRecipeIngredients(userId, recipe)) {
            Ingredient ingredient = recipeIngredient.getIngredient();
            if (shoppingList.containsIngredient(ingredient.getIngredientId())) {
                continue;
            }
            shoppingList.addItem(ShoppingListItem.builder()
                    .ingredient(ingredient)
                    .quantity(recipeIngredient.getQuantity())
                    .unit(recipeIngredient.getUnit())
                    .build());
        }
    }

    // 내 장보기 리스트 전체 조회 (아직 리스트가 없으면 빈 리스트로 응답)
    public MyShoppingListResponse getMyShoppingList(Long userId) {
        return shoppingListRepository.findByUserId(userId)
                .map(MyShoppingListResponse::new)
                .orElseGet(MyShoppingListResponse::empty);
    }

    @Transactional
    public void checkItem(Long userId, Long itemId) {
        findOwnedItem(userId, itemId).check();
    }

    @Transactional
    public void uncheckItem(Long userId, Long itemId) {
        findOwnedItem(userId, itemId).uncheck();
    }

    // 항목 삭제 (안 살 재료 빼기)
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));
        ShoppingListItem item = findOwnedItem(userId, itemId);
        shoppingList.getItems().remove(item);
    }

    private ShoppingListItem findOwnedItem(Long userId, Long itemId) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));

        return shoppingList.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 본인 소유가 아닌 항목입니다. id=" + itemId));
    }

    private List<RecipeIngredient> findMissingRecipeIngredients(Long userId, Recipe recipe) {
        Set<Long> ownedIngredientIds = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중)
                .stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .collect(Collectors.toSet());

        return recipe.getRecipeIngredients().stream()
                .filter(recipeIngredient -> !ownedIngredientIds.contains(recipeIngredient.getIngredient().getIngredientId()))
                .toList();
    }

    // 재료를 직접 검색해서 장보기 리스트에 담기 (셀프 추가)
    @Transactional
    public void addManualItem(Long userId, ManualShoppingItemRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다. ingredientId=" + request.ingredientId()));

        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseGet(() -> shoppingListRepository.save(ShoppingList.builder().userId(userId).build()));

        shoppingList.getItems().stream()
                .filter(item -> item.getIngredient().getIngredientId().equals(ingredient.getIngredientId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addQuantity(request.quantity()),
                        () -> shoppingList.addItem(ShoppingListItem.builder()
                                .ingredient(ingredient)
                                .quantity(request.quantity())
                                .unit(request.unit())
                                .build())
                );
    }
}