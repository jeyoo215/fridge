package com.example.backend.domain.shopping;

import com.example.backend.domain.shopping.dto.CheckAllRequest;
import com.example.backend.domain.shopping.dto.ManualShoppingItemRequest;
import com.example.backend.domain.shopping.dto.MyShoppingListResponse;
import com.example.backend.domain.shopping.dto.QuantityUpdateRequest;
import com.example.backend.domain.shopping.dto.ReorderItemsRequest;
import com.example.backend.domain.shopping.dto.ShoppingListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    // 레시피 기준 부족 재료 미리보기 (기존 기능 유지)
    @GetMapping
    public ShoppingListResponse getShoppingList(@AuthenticationPrincipal Long userId,
                                                 @RequestParam("recipeId") Long recipeId) {
        return shoppingListService.getShoppingList(userId, recipeId);
    }

    // 부족한 재료를 내 장보기 리스트에 담기
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMissingIngredients(@AuthenticationPrincipal Long userId,
                                       @RequestParam("recipeId") Long recipeId) {
        shoppingListService.addMissingIngredientsToMyList(userId, recipeId);
    }

    // 내 장보기 리스트 전체 조회
    @GetMapping("/me")
    public MyShoppingListResponse getMyShoppingList(@AuthenticationPrincipal Long userId) {
        return shoppingListService.getMyShoppingList(userId);
    }

    @PatchMapping("/items/{itemId}/check")
    public void checkItem(@AuthenticationPrincipal Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.checkItem(userId, itemId);
    }

    @PatchMapping("/items/{itemId}/uncheck")
    public void uncheckItem(@AuthenticationPrincipal Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.uncheckItem(userId, itemId);
    }

    @DeleteMapping("/items/{itemId}")
    public void deleteItem(@AuthenticationPrincipal Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.deleteItem(userId, itemId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }

    // 재료를 직접 검색해서 셀프로 담기
    @PostMapping("/items/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public void addManualItem(@AuthenticationPrincipal Long userId, @RequestBody ManualShoppingItemRequest request) {
        shoppingListService.addManualItem(userId, request);
    }

    @PatchMapping("/items/reorder")
    public void reorderItems(@AuthenticationPrincipal Long userId, @RequestBody ReorderItemsRequest request) {
        shoppingListService.reorderItems(userId, request.itemIds());
    }

    @DeleteMapping("/items/checked")
    public void deleteCheckedItems(@AuthenticationPrincipal Long userId) {
        shoppingListService.deleteCheckedItems(userId);
    }

    @DeleteMapping("/items")
    public void deleteAllItems(@AuthenticationPrincipal Long userId) {
        shoppingListService.deleteAllItems(userId);
    }

    @PatchMapping("/items/{itemId}/quantity")
    public void updateQuantity(@AuthenticationPrincipal Long userId, @PathVariable("itemId") Long itemId,
                                @RequestBody QuantityUpdateRequest request) {
        shoppingListService.updateQuantity(userId, itemId, request.quantity());
    }

    @PatchMapping("/items/check-all")
    public void setAllChecked(@AuthenticationPrincipal Long userId, @RequestBody CheckAllRequest request) {
        shoppingListService.setAllChecked(userId, request.checked());
    }

    @PostMapping("/items/purchase")
    public Map<String, List<Long>> purchaseCheckedItems(@AuthenticationPrincipal Long userId) {
        List<Long> createdIds = shoppingListService.purchaseCheckedItems(userId);
        return Map.of("createdUserIngredientIds", createdIds);
    }
}
