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
    private final BigDecimal quantity;
    private final String unit;
    private final LocalDate expirationDate;
    private final long dDay;

    public UserIngredientResponse(UserIngredient entity) {
        this.userIngredientId = entity.getUserIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
        this.expirationDate = entity.getExpirationDate();
        this.dDay = ChronoUnit.DAYS.between(LocalDate.now(), entity.getExpirationDate());
    }
}
