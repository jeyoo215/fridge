package com.example.backend.domain.challenge;

import com.example.backend.domain.challenge.dto.ChallengeResponse;
import com.example.backend.domain.challenge.dto.ChallengeStartRequest;
import com.example.backend.domain.ingredient.UserIngredientRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserIngredientRepository userIngredientRepository;

    // 챌린지 시작 (FR-40)
    @Transactional
    public Long startChallenge(Long userId, ChallengeStartRequest request) {
        challengeRepository.findByUserIdAndStatus(userId, Challenge.Status.진행중)
                .ifPresent(existing -> {
                    throw new IllegalStateException("이미 진행중인 챌린지가 있습니다.");
                });

        LocalDate today = LocalDate.now();
        Challenge challenge = Challenge.builder()
                .userId(userId)
                .startDate(today)
                .endDate(today.plusDays(request.days()))
                .build();

        return challengeRepository.save(challenge).getChallengeId();
    }

    // 챌린지 상태 조회 (기간이 끝났으면 성공/실패 판정까지 함께 처리)
    @Transactional
    public ChallengeResponse getStatus(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 챌린지입니다. id=" + challengeId));

        if (challenge.getStatus() == Challenge.Status.진행중 && challenge.isFinishedPeriod(LocalDate.now())) {
            boolean boughtDuringChallenge = userIngredientRepository
                    .findByUserIdAndStatusOrderByExpirationDateAsc(challenge.getUserId(),
                            com.example.backend.domain.ingredient.UserIngredient.Status.보유중)
                    .stream()
                    .anyMatch(ui -> ui.getPurchaseDate() != null
                            && !ui.getPurchaseDate().isBefore(challenge.getStartDate())
                            && !ui.getPurchaseDate().isAfter(challenge.getEndDate()));

            if (boughtDuringChallenge) {
                challenge.markFailed();
            } else {
                challenge.markSuccess();
            }
        }

        return new ChallengeResponse(challenge);
    }
}