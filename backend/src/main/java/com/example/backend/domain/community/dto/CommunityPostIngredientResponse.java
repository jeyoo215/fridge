package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostIngredient;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CommunityPostIngredientResponse {

    private final Long ingredientId;
    private final String ingredientName;
    private final BigDecimal quantity;
    private final String unit;

    public CommunityPostIngredientResponse(CommunityPostIngredient entity) {
        this.ingredientId = entity.getIngredient().getIngredientId();
        this.ingredientName = entity.getIngredient().getIngredientName();
        this.quantity = entity.getQuantity();
        this.unit = entity.getUnit();
    }
}
