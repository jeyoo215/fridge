package com.example.backend.domain.ingredient.dto;

import com.example.backend.domain.ingredient.UserIngredient;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class UserIngredientResponse {

    private final Long userIngredientId;
    private final String ingredientName;
    private final String categoryName; // 카테고리별 그룹핑용 (없으면 "기타"로 처리)
    private final BigDecimal quantity;
    private final String unit;
    private final LocalDate purchaseDate;
    private final LocalDate expirationDate;
    private final BigDecimal price;
    private final String storageMethod;       // 보관법 안내 (냉장/냉동/실온)
    private final Integer defaultShelfLifeDays;
    private final long dDay;

    public UserIngredientResponse(UserIngredient entity) {
        this.userIngredientId = entity.getUserIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.categoryName = entity.getIngredient().getCategory() != null
                ? entity.getIngredient().getCategory().getCategoryName()
                : "기타";
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
        this.purchaseDate = entity.getPurchaseDate();
        this.expirationDate = entity.getExpirationDate();
        this.price = entity.getPrice();
        this.storageMethod = entity.getIngredient().getStorageMethod() != null
                ? entity.getIngredient().getStorageMethod().name()
                : null;
        this.defaultShelfLifeDays = entity.getIngredient().getDefaultShelfLifeDays();
        this.dDay = ChronoUnit.DAYS.between(LocalDate.now(), entity.getExpirationDate());
    }
}
