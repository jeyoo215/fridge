package com.example.backend.domain.stats.dto;

import java.math.BigDecimal;

// 월간 절약/폐기방지/탄소절감 통계
// savedAmount: 재료에 실제 가격을 입력해뒀으면 그 값의 합, 안 입력한 재료는 평균 추정치로 대체해서 합산.
//              isSavedAmountEstimated=true면 하나 이상 추정치가 섞여있다는 뜻 (화면에 "일부 추정치 포함" 표시용).
// co2ReductionKg는 여전히 무게 데이터가 없어서 항상 추정치임.
public record MonthlyStatsResponse(
        String yearMonth,
        long totalResolvedCount,
        long consumedCount,
        long discardedCount,
        BigDecimal estimatedSavedAmount,
        boolean savedAmountPartiallyEstimated,
        BigDecimal estimatedCo2ReductionKg,
        String equivalentDescription
) {
}
