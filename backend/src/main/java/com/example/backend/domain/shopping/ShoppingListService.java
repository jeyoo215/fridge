package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.ingredient.UserIngredientService;
import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final UserIngredientService userIngredientService;
    private final ShoppingListRepository shoppingListRepository;
    private final IngredientRepository ingredientRepository;

    // 특정 레시피 기준 부족 재료 미리보기
    public ShoppingListResponse getShoppingList(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));

        Set<Long> alreadyInListIngredientIds = shoppingListRepository.findByUserId(userId)
                .map(shoppingList -> shoppingList.getItems().stream()
                        .map(item -> item.getIngredient().getIngredientId())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());

        List<ShoppingListItemResponse> missingIngredients = findMissingRecipeIngredients(userId, recipe).stream()
                .map(recipeIngredient -> new ShoppingListItemResponse(
                        recipeIngredient,
                        alreadyInListIngredientIds.contains(recipeIngredient.getIngredient().getIngredientId())))
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
                ShoppingListItem item = ShoppingListItem.builder()
                        .ingredient(ingredient)
                        .quantity(recipeIngredient.getQuantity())
                        .unit(recipeIngredient.getUnit())
                        .build();
                if (ingredient.isSeasoning()) {
                        item.clearQuantityAndUnit(); // 조미료는 조리용 단위(큰술 등)를 장보기에 그대로 노출하지 않음
                }
                shoppingList.addItem(item);
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

        // 순서 변경
        @Transactional
        public void reorderItems(Long userId, List<Long> orderedItemIds) {
                ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                        .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));

                Map<Long, ShoppingListItem> itemsById = shoppingList.getItems().stream()
                        .collect(Collectors.toMap(ShoppingListItem::getItemId, item -> item));

                for (int i = 0; i < orderedItemIds.size(); i++) {
                        ShoppingListItem item = itemsById.get(orderedItemIds.get(i));
                        if (item == null) {
                        throw new IllegalArgumentException("존재하지 않거나 본인 소유가 아닌 항목이 포함되어 있습니다.");
                        }
                        item.assignDisplayOrderPublic(i + 1); // 아래 7번 참고
                }
        }

        // 체크된 항목 일괄 삭제
        @Transactional
        public void deleteCheckedItems(Long userId) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));
        shoppingList.getItems().removeIf(ShoppingListItem::isChecked);
        }

        // 전체 삭제
        @Transactional
        public void deleteAllItems(Long userId) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));
        shoppingList.getItems().clear();
        }

        // 수량 직접 수정
        @Transactional
        public void updateQuantity(Long userId, Long itemId, BigDecimal quantity) {
        findOwnedItem(userId, itemId).updateQuantity(quantity);
        }

        // 체크된 항목 전체선택/해제
        @Transactional
        public void setAllChecked(Long userId, boolean checked) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));
        shoppingList.getItems().forEach(item -> {
                if (checked) item.check(); else item.uncheck();
        });
        }

        // 체크된 항목들을 보유재료로 등록하고 장보기 리스트에서 제거 ("구매" 버튼)
        @Transactional
        public List<Long> purchaseCheckedItems(Long userId) {
        ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("장보기 리스트가 없습니다."));

        List<ShoppingListItem> checkedItems = shoppingList.getItems().stream()
                .filter(ShoppingListItem::isChecked)
                .toList();

        if (checkedItems.isEmpty()) {
                throw new IllegalArgumentException("구매 처리할 항목이 없습니다. 먼저 항목을 체크해주세요.");
        }

        LocalDate today = LocalDate.now();
        List<Long> createdUserIngredientIds = new ArrayList<>();

        for (ShoppingListItem item : checkedItems) {
                Ingredient ingredient = item.getIngredient();
                BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
                // 기본 소비기한 정보 없는 재료는 7일로 (프론트 재료등록 화면 촬영등록 시 쓰는 기본값과 동일)
                int shelfLifeDays = ingredient.getDefaultShelfLifeDays() != null ? ingredient.getDefaultShelfLifeDays() : 7;

                UserIngredientRegisterRequest registerRequest = new UserIngredientRegisterRequest(
                        ingredient.getIngredientId(), quantity, item.getUnit(), today, today.plusDays(shelfLifeDays)
                );
                createdUserIngredientIds.add(userIngredientService.register(userId, registerRequest));
        }

        shoppingList.getItems().removeAll(checkedItems);
        return createdUserIngredientIds;
        }
}