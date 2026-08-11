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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    // ⚠️ 재료에 실제 가격을 입력 안 했을 때만 쓰는 평균 추정치.
    //    탄소 절감량은 무게 데이터가 아예 없어서 항상 이 상수로 추정함.
    private static final BigDecimal AVG_PRICE_PER_ITEM_WON = BigDecimal.valueOf(2000);
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

        // 절약 금액: 폐기 안 하고 다 쓴(소진) 재료 기준
        AmountResult savedResult = sumAmount(consumedItems);
        // 낭비 금액: 상해서 버린(폐기) 재료 기준
        AmountResult wastedResult = sumAmount(discardedItems);
        boolean anyEstimated = savedResult.hasEstimated() || wastedResult.hasEstimated();

        BigDecimal estimatedCo2ReductionKg = AVG_CO2_PER_ITEM_KG
                .multiply(BigDecimal.valueOf(consumedItems.size()))
                .setScale(2, RoundingMode.HALF_UP);

        String equivalentDescription = buildEquivalentDescription(estimatedCo2ReductionKg);

        return new MonthlyStatsResponse(
                yearMonth.toString(),
                consumedItems.size() + discardedItems.size(),
                consumedItems.size(),
                discardedItems.size(),
                savedResult.total(),
                wastedResult.total(),
                anyEstimated,
                estimatedCo2ReductionKg,
                equivalentDescription
        );
    }

    // 재료 목록의 가격을 합산 (실제 가격 있으면 그대로, 없으면 평균 추정치로 대체)
    private AmountResult sumAmount(List<UserIngredient> items) {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasEstimated = false;
        for (UserIngredient item : items) {
            if (item.getPrice() != null) {
                total = total.add(item.getPrice());
            } else {
                total = total.add(AVG_PRICE_PER_ITEM_WON);
                hasEstimated = true;
            }
        }
        return new AmountResult(total, hasEstimated);
    }

    private record AmountResult(BigDecimal total, boolean hasEstimated) {
    }

    private String buildEquivalentDescription(BigDecimal co2ReductionKg) {
        if (co2ReductionKg.compareTo(BigDecimal.ZERO) <= 0) {
            return "아직 절감 기록이 없어요. 재료를 다 써보세요!";
        }
        BigDecimal km = co2ReductionKg.divide(CAR_CO2_PER_KM_KG, 1, RoundingMode.HALF_UP);
        return "자동차로 약 " + km + "km 안 탄 것과 비슷한 효과예요";
    }
}
