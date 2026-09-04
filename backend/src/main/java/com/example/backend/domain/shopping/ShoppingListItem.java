package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    // 리스트 내 표시 순서 (드래그 정렬용). 기존 row는 마이그레이션 시 null로 남을 수 있어 nullable로 둠.
    @Column(name = "display_order")
    private Integer displayOrder;

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

    void assignDisplayOrder(int order) {
        this.displayOrder = order;
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

    public void updateQuantity(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = newQuantity;
    }

    // 조미료 등, 장보기에서는 단위 표시가 어색한 재료를 담을 때 사용
    public void clearQuantityAndUnit() {
        this.quantity = null;
        this.unit = null;
    }

    public void assignDisplayOrderPublic(int order) {
        this.displayOrder = order;
    }
}