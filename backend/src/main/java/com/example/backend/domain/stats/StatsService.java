package com.example.backend.domain.stats;

import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.stats.dto.MonthlyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    // ⚠️ 무게 데이터가 없어서 탄소 절감량은 여전히 이 상수 기반 평균 추정치임.
    //    가격 기반 통계(절약/낭비 금액)는 팀 논의 결과 제외함 (추정치 신뢰도 문제).
    private static final BigDecimal AVG_CO2_PER_ITEM_KG = BigDecimal.valueOf(0.3);
    private static final BigDecimal CAR_CO2_PER_KM_KG = BigDecimal.valueOf(0.12);

    private final UserIngredientRepository userIngredientRepository;

    public MonthlyStatsResponse getMonthlyStats(Long userId, String yearMonthStr) {
        YearMonth yearMonth = (yearMonthStr == null || yearMonthStr.isBlank())
                ? YearMonth.now()
                : YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UserIngredient> resolved = userIngredientRepository
                .findByUserIdAndResolvedAtBetween(userId, start, end);

        List<UserIngredient> consumedItems = resolved.stream()
                .filter(i -> i.getStatus() == UserIngredient.Status.소진)
                .toList();
        List<UserIngredient> discardedItems = resolved.stream()
                .filter(i -> i.getStatus() == UserIngredient.Status.폐기)
                .toList();

        BigDecimal estimatedCo2ReductionKg = AVG_CO2_PER_ITEM_KG
                .multiply(BigDecimal.valueOf(consumedItems.size()))
                .setScale(2, RoundingMode.HALF_UP);

        String equivalentDescription = buildEquivalentDescription(estimatedCo2ReductionKg);

        // 가장 많이 버린 재료 계산 (이름별로 세서 최다인 것 하나)
        Map<String, Long> discardCountByName = discardedItems.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getIngredient().getIngredientName(),
                        Collectors.counting()
                ));
        Map.Entry<String, Long> topDiscarded = discardCountByName.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        List<MonthlyStatsResponse.CategoryDiscardStat> categoryDiscardStats = buildCategoryDiscardStats(discardedItems);

        return new MonthlyStatsResponse(
                yearMonth.toString(),
                consumedItems.size() + discardedItems.size(),
                consumedItems.size(),
                discardedItems.size(),
                estimatedCo2ReductionKg,
                equivalentDescription,
                topDiscarded != null ? topDiscarded.getKey() : null,
                topDiscarded != null ? topDiscarded.getValue() : 0,
                categoryDiscardStats
        );
    }

    // 카테고리별 폐기 개수를 많은 순으로 정리 (카테고리 없는 재료는 "기타"로 묶음)
    private List<MonthlyStatsResponse.CategoryDiscardStat> buildCategoryDiscardStats(List<UserIngredient> discardedItems) {
        Map<String, Long> countByCategory = discardedItems.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getIngredient().getCategory() != null
                                ? i.getIngredient().getCategory().getCategoryName()
                                : "기타",
                        Collectors.counting()
                ));

        return countByCategory.entrySet().stream()
                .map(e -> new MonthlyStatsResponse.CategoryDiscardStat(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(MonthlyStatsResponse.CategoryDiscardStat::discardedCount).reversed())
                .toList();
    }

    private String buildEquivalentDescription(BigDecimal co2ReductionKg) {
        if (co2ReductionKg.compareTo(BigDecimal.ZERO) <= 0) {
            return "아직 절감 기록이 없어요. 재료를 다 써보세요!";
        }
        BigDecimal km = co2ReductionKg.divide(CAR_CO2_PER_KM_KG, 1, RoundingMode.HALF_UP);
        return "자동차로 약 " + km + "km 안 탄 것과 비슷한 효과예요";
    }
}
