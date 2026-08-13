package com.example.backend.domain.shopping.dto;

import java.math.BigDecimal;

// 셀프 추가용 요청 (재료 마스터 검색 결과에서 ingredientId를 골라서 보냄)
public record ManualShoppingItemRequest(
        Long ingredientId,
        BigDecimal quantity,
        String unit
) {}