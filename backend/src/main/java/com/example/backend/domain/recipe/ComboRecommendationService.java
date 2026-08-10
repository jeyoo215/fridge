package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.ComboRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComboRecommendationService {

    private final ComboRecommendationRepository comboRecommendationRepository;

    // 의외의 재료 조합 추천 조회 - Python 배치가 미리 계산해둔 값을 읽기만 함 (FR-23)
    public List<ComboRecommendResponse> getComboRecommendations(Long userId) {
        return comboRecommendationRepository.findByUserIdOrderByComboScoreDesc(userId).stream()
                .map(ComboRecommendResponse::new)
                .toList();
    }
}