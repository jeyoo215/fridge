package com.example.backend.domain.fridge.dto;

import com.example.backend.domain.fridge.FridgeItem;

import java.time.LocalDate;

public record FridgeItemResponse(
        Long fridgeItemId,
        Long userIngredientId,
        String ingredientName,
        LocalDate expirationDate,
        String imageUrl,
        String imageType,
        Double posX,
        Double posY,
        String zone
) {
    public FridgeItemResponse(FridgeItem fi) {
        this(
            fi.getFridgeItemId(),
            fi.getUserIngredient().getUserIngredientId(),
            fi.getUserIngredient().getIngredient().getIngredientName(),
            fi.getUserIngredient().getExpirationDate(),
            fi.getImageUrl(),
            fi.getImageType() == null ? null : fi.getImageType().name(),
            fi.getPosX(),
            fi.getPosY(),
            fi.getZone() == null ? null : fi.getZone().name()
        );
    }
}