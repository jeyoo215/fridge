// dto/MyShoppingListResponse.java
package com.example.backend.domain.shopping.dto;

import com.example.backend.domain.shopping.ShoppingList;
import lombok.Getter;
import java.util.List;

@Getter
public class MyShoppingListResponse {
    private final Long shoppingListId;
    private final List<ShoppingListItemDetailResponse> items;

    public MyShoppingListResponse(ShoppingList entity) {
        this.shoppingListId = entity.getShoppingListId();
        this.items = entity.getItems().stream().map(ShoppingListItemDetailResponse::new).toList();
    }

    private MyShoppingListResponse(Long shoppingListId, List<ShoppingListItemDetailResponse> items) {
        this.shoppingListId = shoppingListId;
        this.items = items;
    }

    public static MyShoppingListResponse empty() {
        return new MyShoppingListResponse(null, List.of());
    }
}