package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.ComboRecommendResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComboRecommendationServiceTest {

    @Mock private ComboRecommendationRepository comboRecommendationRepository;

    @InjectMocks
    private ComboRecommendationService comboRecommendationService;

    @Test
    @DisplayName("사용자의 조합 추천 목록을 응답 DTO로 변환해서 반환한다 (FR-23)")
    void getComboRecommendations_정상조회() {
        Recipe recipe = Recipe.builder().recipeName("두부계란찜").build();
        ReflectionTestUtils.setField(recipe, "recipeId", 3L);

        ComboRecommendation combo = new ComboRecommendation();
        ReflectionTestUtils.setField(combo, "userId", 1L);
        ReflectionTestUtils.setField(combo, "recipe", recipe);
        ReflectionTestUtils.setField(combo, "comboScore", 6.0);
        ReflectionTestUtils.setField(combo, "generatedAt", LocalDateTime.now());

        when(comboRecommendationRepository.findByUserIdOrderByComboScoreDesc(1L))
                .thenReturn(List.of(combo));

        List<ComboRecommendResponse> result = comboRecommendationService.getComboRecommendations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipeId()).isEqualTo(3L);
        assertThat(result.get(0).getRecipeName()).isEqualTo("두부계란찜");
        assertThat(result.get(0).getComboScore()).isEqualTo(6.0);
    }

    @Test
    @DisplayName("배치가 아직 계산 안 했거나 추천 이력이 없으면 빈 리스트를 반환한다")
    void getComboRecommendations_없으면_빈리스트() {
        when(comboRecommendationRepository.findByUserIdOrderByComboScoreDesc(1L))
                .thenReturn(List.of());

        List<ComboRecommendResponse> result = comboRecommendationService.getComboRecommendations(1L);

        assertThat(result).isEmpty();
    }
}