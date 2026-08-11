package com.example.backend.domain.stats.dto;

import java.math.BigDecimal;

// 월간 절약/폐기방지/탄소절감 통계
// savedAmount: 소진(다 쓴) 재료 기준 절약 추정 금액. wastedAmount: 폐기한 재료 기준 낭비 추정 금액.
//              둘 다 실제 가격 입력해뒀으면 그 값, 안 입력했으면 평균 추정치로 대체.
//              partiallyEstimated=true면 그중 하나 이상이 추정치라는 뜻 (화면에 "일부 추정치 포함" 표시용).
// co2ReductionKg는 여전히 무게 데이터가 없어서 항상 추정치임.
public record MonthlyStatsResponse(
        String yearMonth,
        long totalResolvedCount,
        long consumedCount,
        long discardedCount,
        BigDecimal estimatedSavedAmount,
        BigDecimal estimatedWastedAmount,
        boolean amountPartiallyEstimated,
        BigDecimal estimatedCo2ReductionKg,
        String equivalentDescription,
        String mostDiscardedIngredientName, // 이번 달 가장 많이 버린 재료 이름 (없으면 null)
        long mostDiscardedCount
) {
}
