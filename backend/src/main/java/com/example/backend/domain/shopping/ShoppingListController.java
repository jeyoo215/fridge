package com.example.backend.domain.shopping;

import com.example.backend.domain.shopping.dto.MyShoppingListResponse;
import com.example.backend.domain.shopping.dto.ShoppingListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.

    // 레시피 기준 부족 재료 미리보기 (기존 기능 유지)
    @GetMapping
    public ShoppingListResponse getShoppingList(@RequestParam("userId") Long userId, @RequestParam("recipeId") Long recipeId) {
        return shoppingListService.getShoppingList(userId, recipeId);
    }

    // 부족한 재료를 내 장보기 리스트에 담기
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMissingIngredients(@RequestParam("userId") Long userId, @RequestParam("recipeId") Long recipeId) {
        shoppingListService.addMissingIngredientsToMyList(userId, recipeId);
    }

    // 내 장보기 리스트 전체 조회
    @GetMapping("/me")
    public MyShoppingListResponse getMyShoppingList(@RequestParam("userId") Long userId) {
        return shoppingListService.getMyShoppingList(userId);
    }

    @PatchMapping("/items/{itemId}/check")
    public void checkItem(@RequestParam Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.checkItem(userId, itemId);
    }

    @PatchMapping("/items/{itemId}/uncheck")
    public void uncheckItem(@RequestParam Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.uncheckItem(userId, itemId);
    }

    @DeleteMapping("/items/{itemId}")
    public void deleteItem(@RequestParam Long userId, @PathVariable("itemId") Long itemId) {
        shoppingListService.deleteItem(userId, itemId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}