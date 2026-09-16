// dto/ShoppingListItemDetailResponse.java
package com.example.backend.domain.shopping.dto;

import com.example.backend.domain.shopping.ShoppingListItem;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class ShoppingListItemDetailResponse {
    private final Long itemId;
    private final Long ingredientId;
    private final String ingredientName;
    private final BigDecimal quantity;
    private final String unit;
    private final boolean checked;
    private final Integer displayOrder;

    public ShoppingListItemDetailResponse(ShoppingListItem entity) {
        this.itemId = entity.getItemId();
        this.ingredientId = entity.getIngredient().getIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
        this.checked = entity.isChecked();
        this.displayOrder = entity.getDisplayOrder();
    }
}