package com.example.backend.domain.stats.dto;

import java.math.BigDecimal;
import java.util.List;

// 월간 폐기방지/탄소절감 통계
// ⚠️ 팀 논의 결과 가격 기반 통계(절약/낭비 금액)는 추정치 신뢰도 문제로 제외함. 개수 기반 통계만 제공.
// co2ReductionKg는 무게 데이터가 없어서 여전히 평균 추정치임.
public record MonthlyStatsResponse(
        String yearMonth,
        long totalResolvedCount,
        long consumedCount,
        long discardedCount,
        BigDecimal estimatedCo2ReductionKg,
        String equivalentDescription,
        String mostDiscardedIngredientName, // 이번 달 가장 많이 버린 재료 이름 (없으면 null)
        long mostDiscardedCount,
        List<CategoryDiscardStat> categoryDiscardStats // 카테고리별 폐기 개수 (많은 순)
) {
    public record CategoryDiscardStat(String categoryName, long discardedCount) {
    }
}
