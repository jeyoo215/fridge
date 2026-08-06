package com.example.backend.domain.challenge;

import com.example.backend.domain.challenge.dto.ChallengeResponse;
import com.example.backend.domain.challenge.dto.ChallengeStartRequest;
import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserIngredientRepository userIngredientRepository;

    @InjectMocks
    private ChallengeService challengeService;

    @Test
    @DisplayName("이미 진행중인 챌린지가 있으면 새로 시작할 수 없다")
    void startChallenge_이미진행중이면_예외() {
        when(challengeRepository.findByUserIdAndStatus(1L, Challenge.Status.진행중))
                .thenReturn(Optional.of(Challenge.builder().userId(1L).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(7)).build()));

        assertThatThrownBy(() -> challengeService.startChallenge(1L, new ChallengeStartRequest(7)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("기간 중 새로 구매한 재료가 없으면 성공으로 판정된다")
    void getStatus_구매없으면_성공() {
        Challenge challenge = Challenge.builder()
                .userId(1L)
                .startDate(LocalDate.now().minusDays(8))
                .endDate(LocalDate.now().minusDays(1)) // 이미 종료됨
                .build();
        ReflectionTestUtils.setField(challenge, "challengeId", 1L);

        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challenge));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of()); // 챌린지 기간 중 구매 이력 없음

        ChallengeResponse response = challengeService.getStatus(1L);

        assertThat(response.getStatus()).isEqualTo("성공");
    }

    @Test
    @DisplayName("기간 중 새로 구매한 재료가 있으면 실패로 판정된다")
    void getStatus_구매있으면_실패() {
        LocalDate start = LocalDate.now().minusDays(8);
        LocalDate end = LocalDate.now().minusDays(1);

        Challenge challenge = Challenge.builder().userId(1L).startDate(start).endDate(end).build();
        ReflectionTestUtils.setField(challenge, "challengeId", 1L);

        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        UserIngredient boughtDuringChallenge = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(start.plusDays(2)) // 챌린지 기간 중 구매
                .expirationDate(end.plusDays(5))
                .build();

        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challenge));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(boughtDuringChallenge));

        ChallengeResponse response = challengeService.getStatus(1L);

        assertThat(response.getStatus()).isEqualTo("실패");
    }
}