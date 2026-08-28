package com.example.backend.domain.fridge.dto;

import com.example.backend.domain.fridge.FridgeItem;

// 이미 등록된 보유재료를 냉장고에 배치
public record FridgeItemPlaceRequest(
        Long userIngredientId,    // 기존 보유재료 id
        String imageUrl,
        FridgeItem.ImageType imageType,
        Double posX,
        Double posY,
        FridgeItem.Zone zone
) {
}