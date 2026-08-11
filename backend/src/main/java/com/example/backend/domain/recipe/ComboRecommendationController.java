package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.ComboRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComboRecommendationController {

    private final ComboRecommendationService comboRecommendationService;
    private final ComboRecommendationScheduler comboRecommendationScheduler;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @GetMapping("/api/v1/recipes/combo-recommend")
    public List<ComboRecommendResponse> getComboRecommendations(@RequestParam("userId") Long userId) {
        return comboRecommendationService.getComboRecommendations(userId);
    }

    // 관리자용 수동 재계산 트리거. 매일 새벽 스케줄까지 안 기다리고 지금 바로 배치를 돌리고 싶을 때 사용.
    // 배치는 비동기로 실행되므로 이 요청은 배치 완료를 기다리지 않고 바로 202를 반환한다.
    // TODO: 관리자 인증(JWT + 권한 체크) 붙기 전까지는 아무나 호출 가능한 상태 - 배포 전 반드시 보호 필요.
    @PostMapping("/api/v1/admin/combo-recommend/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void refreshComboRecommendations() {
        comboRecommendationScheduler.runNowAsync();
    }
}