package com.example.backend.domain.fridge.dto;

import com.example.backend.domain.fridge.FridgeItem;

import java.math.BigDecimal;
import java.time.LocalDate;

// 냉장고에 재료 새로 등록하면서 배치 (재료 등록 + 위치 저장 한 번에)
public record FridgeItemCreateRequest(
        Long ingredientId,        // 재료 마스터 id (자동완성으로 고른 것)
        BigDecimal quantity,
        String unit,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        String imageUrl,
        FridgeItem.ImageType imageType,
        Double posX,
        Double posY,
        FridgeItem.Zone zone
) {
}