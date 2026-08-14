package com.example.backend.domain.recipe;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ComboRecommendationScheduler {

    @Value("${combo.batch.enabled:true}")
    private boolean enabled;

    @Value("${combo.batch.python-path:python}")
    private String pythonPath;

    @Value("${combo.batch.script-path:./ml/calculate_combo_recommendation.py}")
    private String scriptPath;

    @Value("${combo.batch.run-on-startup:true}")
    private boolean runOnStartup;

    @PostConstruct
    public void checkPythonAvailable() {
        if (!enabled) {
            log.info("combo.batch.enabled=false - 조합 추천 배치 파이썬 환경 체크를 건너뜀");
            return;
        }
        try {
            Process process = new ProcessBuilder(pythonPath, "--version").start();
            process.waitFor();
        } catch (Exception e) {
            log.warn("⚠️ combo.batch.python-path='{}' 실행 확인 실패 - 조합 추천 배치가 새벽마다 조용히 " +
                    "실패할 수 있습니다. 파이썬 환경이 없으면 application.properties에서 " +
                    "combo.batch.enabled=false로 꺼두세요.", pythonPath);
        }
    }

    // 매일 새벽에 자동 실행 (전체 유저 대상, 새 레시피 반영용)
    @Scheduled(cron = "${combo.batch.cron:0 0 3 * * *}")
    public void runScheduled() {
        if (!enabled) {
            log.info("combo.batch.enabled=false 라서 조합 추천 배치를 건너뜀");
            return;
        }
        executeBatch(null);
    }

    // 서버 시작 직후 1회 전체 실행
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartupBatch() {
        if (!enabled || !runOnStartup) {
            log.info("서버 시작 시 조합 추천 배치 자동 실행을 건너뜀 (enabled={}, runOnStartup={})", enabled, runOnStartup);
            return;
        }
        log.info("서버 시작 시 조합 추천 배치 자동 실행");
        executeBatch(null);
    }

    // 관리자용 수동 전체 재계산
    @Async
    public void runNowAsync() {
        log.info("관리자 요청으로 조합 추천 배치 전체 재계산");
        executeBatch(null);
    }

    // 리뷰 등록 등으로 특정 유저만 재계산하고 싶을 때
    @Async
    public void runNowAsync(Long userId) {
        log.info("유저별 조합 추천 재계산 트리거 (userId={})", userId);
        executeBatch(userId);
    }

    // 실제 배치 실행 - userId가 null이면 전체, 있으면 그 유저만 (calculate_combo_recommendation.py 인자로 전달)
    private void executeBatch(Long userId) {
        log.info("의외의 재료 조합 추천 배치 시작 (python={}, script={}, userId={})", pythonPath, scriptPath, userId);
        try {
            List<String> command = new ArrayList<>(List.of(pythonPath, scriptPath));
            if (userId != null) {
                command.add(String.valueOf(userId));
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[combo-batch] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("의외의 재료 조합 추천 배치 완료");
            } else {
                log.error("의외의 재료 조합 추천 배치 실패 (exitCode={})", exitCode);
            }
        } catch (Exception e) {
            log.error("의외의 재료 조합 추천 배치 실행 중 오류 발생", e);
        }
    }
}