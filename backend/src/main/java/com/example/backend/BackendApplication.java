package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// EnableScheduling: 의외의 재료 조합 추천 배치(ComboRecommendationScheduler) 자동 실행용 (FR-23)
// EnableAsync: 관리자 수동 트리거 API가 배치 끝날 때까지 응답을 붙잡고 있지 않도록 비동기 실행용
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}