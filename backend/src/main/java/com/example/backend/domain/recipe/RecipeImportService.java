package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.api.CookRcpResponse;
import com.example.backend.domain.recipe.dto.api.CookRcpRow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private static final String SOURCE = "식약처";
    private static final String SERVICE_ID = "COOKRCP01";
    private static final int BATCH_SIZE = 1000; // 식약처 API 한 번 호출 최대 건수
    private final RecipeParsingWorker worker;

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;

    @Value("${foodsafety.api.key}")
    private String apiKey;

    @Value("${foodsafety.api.base-url}")
    private String baseUrl;

    // 전체 레시피 수집 (1건씩 저장, 이미 있으면 건너뜀)
    @Transactional
    public int importAll() {
        RestClient restClient = RestClient.create();
        int savedCount = 0;
        int start = 1;

        while (true) {
            int end = start + BATCH_SIZE - 1;

            // 요청 URL: {base}/{key}/COOKRCP01/json/{start}/{end}
            String url = String.format("%s/%s/%s/json/%d/%d",
                    baseUrl, apiKey, SERVICE_ID, start, end);
            log.info("수집 URL: {}", url); // 이 줄 추가

            CookRcpResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CookRcpResponse.class);

            // 응답이 비어있으면 종료
            if (response == null || response.getCookRcp01() == null
                    || response.getCookRcp01().getRow() == null
                    || response.getCookRcp01().getRow().isEmpty()) {
                break;
            }

            List<CookRcpRow> rows = response.getCookRcp01().getRow();
            for (CookRcpRow row : rows) {
                if (saveRecipe(row)) {
                    savedCount++;
                }
            }

            // 이번 배치가 BATCH_SIZE보다 적게 왔으면 마지막 페이지 → 종료
            if (rows.size() < BATCH_SIZE) {
                break;
            }
            start += BATCH_SIZE;
        }

        log.info("레시피 수집 완료: {}건 신규 저장", savedCount);
        return savedCount;
    }

    // 레시피 한 건 저장 (이미 있으면 false, 새로 저장하면 true)
    private boolean saveRecipe(CookRcpRow row) {
        String externalId = row.getRcpSeq();

        // 중복 확인
        if (recipeRepository.existsBySourceAndExternalId(SOURCE, externalId)) {
            return false;
        }

        // 카테고리: RCP_PAT2로 찾거나 없으면 생성
        RecipeCategory category = resolveCategory(row.getRcpPat2());

        Recipe recipe = Recipe.builder()
                .category(category)
                .recipeName(row.getRcpNm())
                .rawIngredients(row.getRcpPartsDtls()) // 재료 원문 그대로 보존
                .imageUrl(row.getAttFileNoMain())
                .source(SOURCE)
                .externalId(externalId)
                .calorie(parseDouble(row.getInfoEng()))
                .carbohydrate(parseDouble(row.getInfoCar()))
                .protein(parseDouble(row.getInfoPro()))
                .fat(parseDouble(row.getInfoFat()))
                .sodium(parseDouble(row.getInfoNa()))
                .build();

        recipeRepository.save(recipe);
        return true;
    }

    // 카테고리 "있으면 가져오고 없으면 생성"
    private RecipeCategory resolveCategory(String categoryName) {
        // RCP_PAT2가 비어있으면 "기타"로 처리
        String name = (categoryName == null || categoryName.isBlank()) ? "기타" : categoryName;

        return recipeCategoryRepository.findByCategoryName(name)
                .orElseGet(() -> recipeCategoryRepository.save(
                        RecipeCategory.builder().categoryName(name).build()));
    }

    // 문자열 → Double 변환 (빈 값이나 변환 실패 시 null)
    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 식약처 API 다시 받아서 조리순서만 채움
    public int importCookingSteps() {
        RestClient restClient = RestClient.create();
        int savedCount = 0;
        int start = 1;

        while (true) {
            int end = start + BATCH_SIZE - 1;
            String url = String.format("%s/%s/%s/json/%d/%d", baseUrl, apiKey, SERVICE_ID, start, end);

            var response = restClient.get().uri(url).retrieve()
                    .body(com.example.backend.domain.recipe.dto.api.CookRcpResponse.class);

            if (response == null || response.getCookRcp01() == null
                    || response.getCookRcp01().getRow() == null
                    || response.getCookRcp01().getRow().isEmpty()) {
                break;
            }

            var rows = response.getCookRcp01().getRow();
            log.info("import-steps: rows={}", rows == null ? "null" : rows.size());
            for (var row : rows) {
                try {
                    if (worker.saveSteps(row.getRcpSeq(), row.getSteps())) {
                        savedCount++;
                    }
                } catch (Exception e) {
                    log.error("조리순서 저장 실패 [{}]: {}", row.getRcpSeq(), e.getMessage());
                }
            }

            if (rows.size() < BATCH_SIZE)
                break;
            start += BATCH_SIZE;
        }

        log.info("조리순서 수집 완료: {}건", savedCount);
        return savedCount;
    }
}