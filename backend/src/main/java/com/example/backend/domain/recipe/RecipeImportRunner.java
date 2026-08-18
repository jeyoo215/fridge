package com.example.backend.domain.recipe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 서버 시작 시 레시피 데이터가 비어있으면 자동으로 1회 수집
@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeImportRunner implements CommandLineRunner {

    private final RecipeRepository recipeRepository;
    private final RecipeImportService recipeImportService;

    @Override
    public void run(String... args) {
        // 이미 레시피가 있으면 수집 건너뜀 (서버 껐다 켤 때마다 재수집 방지)
        long count = recipeRepository.count();
        if (count > 0) {
            log.info("레시피가 이미 {}건 존재합니다 수집을 건너뜁니다.", count);
            return;
        }

        log.info("레시피 데이터가 비어있어 식약처 API 수집을 시작합니다...");
        int saved = recipeImportService.importAll();
        log.info("식약처 API 수집 종료. 신규 저장: {}건", saved);
    }
}