package com.example.backend.domain.shopping;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ERD의 장보기리스트 테이블. 사용자당 진행중인 리스트 1개를 유지한다 (FR-30)
@Entity
@Table(name = "shopping_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_list_id")
    private Long shoppingListId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingListItem> items = new ArrayList<>();

    @Builder
    public ShoppingList(Long userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(ShoppingListItem item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public boolean containsIngredient(Long ingredientId) {
        return items.stream()
                .anyMatch(item -> item.getIngredient().getIngredientId().equals(ingredientId));
    }
}