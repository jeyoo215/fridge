package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.ComboRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComboRecommendationController {

    private final ComboRecommendationService comboRecommendationService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @GetMapping("/api/v1/recipes/combo-recommend")
    public List<ComboRecommendResponse> getComboRecommendations(@RequestParam("userId") Long userId) {
        return comboRecommendationService.getComboRecommendations(userId);
    }
}