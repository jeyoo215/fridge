package com.example.backend.domain.recipe;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// ml/calculate_combo_recommendation.py(Apriori 연관 규칙 배치)를 자동으로 재실행해서
// combination_recommendation 테이블을 최신 상태로 유지한다 (FR-23).
// 실제 계산/저장은 Python 쪽이 다 하고, 여기서는 그 프로세스를 실행만 시켜준다.
@Slf4j
@Component
public class ComboRecommendationScheduler {

    @Value("${combo.batch.enabled:true}")
    private boolean enabled;

    @Value("${combo.batch.python-path:python}")
    private String pythonPath;

    @Value("${combo.batch.script-path:../ml/calculate_combo_recommendation.py}")
    private String scriptPath;

    // 앱 부팅 시 파이썬 실행 파일이 실제로 동작하는지 미리 확인.
    // 여기서 실패해도 앱을 죽이진 않음 - 그냥 "이대로 두면 새벽마다 조용히 실패한다"고 미리 경고만 함.
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

    // 매일 새벽에 자동 실행 (cron은 application.properties의 combo.batch.cron 에서 설정)
    @Scheduled(cron = "${combo.batch.cron:0 0 3 * * *}")
    public void runScheduled() {
        if (!enabled) {
            log.info("combo.batch.enabled=false 라서 조합 추천 배치를 건너뜀");
            return;
        }
        executeBatch();
    }

    // 관리자가 수동으로 지금 바로 재계산하고 싶을 때 호출 (ComboRecommendationController).
    // @Async라서 배치가 끝날 때까지 HTTP 응답을 붙잡고 있지 않음.
    @Async
    public void runNowAsync() {
        log.info("관리자 요청으로 조합 추천 배치 수동 실행");
        executeBatch();
    }

    private void executeBatch() {
        log.info("의외의 재료 조합 추천 배치 시작 (python={}, script={})", pythonPath, scriptPath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath);
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
            // 배치가 실패해도 서버 자체는 절대 죽으면 안 됨 - 로그만 남기고 다음 스케줄에 재시도
            log.error("의외의 재료 조합 추천 배치 실행 중 오류 발생", e);
        }
    }
}