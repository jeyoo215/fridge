package com.example.backend.domain.shopping;

import com.example.backend.domain.shopping.dto.ShoppingListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: GET /api/v1/shopping-list?userId=1&recipeId=1
    @GetMapping
    public ShoppingListResponse getShoppingList(@RequestParam Long userId, @RequestParam Long recipeId) {
        return shoppingListService.getShoppingList(userId, recipeId);
    }
}