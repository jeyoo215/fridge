package com.example.backend.domain.ingredient.dto;

import com.example.backend.domain.ingredient.UserIngredient;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Entity를 그대로 화면에 내려주지 않고, 화면에 필요한 형태로 가공해서 내려주는 DTO
@Getter
public class UserIngredientResponse {

    private final Long userIngredientId;
    private final String ingredientName;
    private final BigDecimal quantity;
    private final String unit;
    private final LocalDate expirationDate;
    private final long dDay; // 오늘 기준 유통기한까지 남은 일수 (프론트에서 D-1, D-3 표시용)

    public UserIngredientResponse(UserIngredient entity) {
        this.userIngredientId = entity.getUserIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
        this.expirationDate = entity.getExpirationDate();
        this.dDay = ChronoUnit.DAYS.between(LocalDate.now(), entity.getExpirationDate());
    }
}
