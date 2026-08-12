package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// ERD의 리스트아이템 테이블 (장보기 리스트에 담긴 개별 재료)
@Entity
@Table(name = "shopping_list_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 10)
    private String unit;

    @Column(nullable = false)
    private boolean checked;

    @Builder
    public ShoppingListItem(Ingredient ingredient, BigDecimal quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = false;
    }

    void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public void check() {
        this.checked = true;
    }

    public void uncheck() {
        this.checked = false;
    }

    public void addQuantity(BigDecimal amount) {
        if (amount == null) return;
        this.quantity = (this.quantity == null) ? amount : this.quantity.add(amount);
    }
}