package com.example.backend.domain.challenge;

import com.example.backend.domain.badge.BadgeService;
import com.example.backend.domain.challenge.dto.ChallengeResponse;
import com.example.backend.domain.challenge.dto.ChallengeStartRequest;
import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final BadgeService badgeService;

    // 챌린지 시작 (FR-40)
    @Transactional
    public Long startChallenge(Long userId, ChallengeStartRequest request) {
        challengeRepository.findByUserIdAndStatus(userId, Challenge.Status.진행중)
                .ifPresent(existing -> {
                    throw new IllegalStateException("이미 진행중인 챌린지가 있습니다.");
                });

        Challenge.ChallengeType type = parseType(request.type());

        if (type == Challenge.ChallengeType.TARGET_INGREDIENT
                && (request.targetIngredientIds() == null || request.targetIngredientIds().isEmpty())) {
            throw new IllegalArgumentException("특정 재료 소진 챌린지는 재료를 하나 이상 선택해야 합니다.");
        }

        LocalDate today = LocalDate.now();
        Challenge challenge = Challenge.builder()
                .userId(userId)
                .startDate(today)
                .endDate(today.plusDays(request.days()))
                .type(type)
                .build();

        if (type == Challenge.ChallengeType.TARGET_INGREDIENT) {
            for (Long ingredientId : request.targetIngredientIds()) {
                if (!ingredientRepository.existsById(ingredientId)) {
                    throw new IllegalArgumentException("존재하지 않는 재료입니다. ingredientId=" + ingredientId);
                }
                challenge.addTargetIngredient(ingredientId);
            }
        }

        return challengeRepository.save(challenge).getChallengeId();
    }

    private Challenge.ChallengeType parseType(String type) {
        try {
            return Challenge.ChallengeType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 챌린지 타입입니다. type=" + type);
        }
    }

    // 챌린지 상태 조회 (기간이 끝났으면 성공/실패 판정까지 함께 처리)
    @Transactional
    public ChallengeResponse getStatus(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 챌린지입니다. id=" + challengeId));

        if (challenge.getStatus() == Challenge.Status.진행중 && challenge.isFinishedPeriod(LocalDate.now())) {
            boolean success = switch (challenge.getType()) {
                case FRIDGE_CLEAN -> judgeFridgeClean(challenge);
                case TARGET_INGREDIENT -> judgeTargetIngredientConsumed(challenge);
            };

            if (success) {
                challenge.markSuccess();
                badgeService.onChallengeSuccess(challenge.getUserId());
            } else {
                challenge.markFailed();
                badgeService.onChallengeFailed(challenge.getUserId());
            }
        }

        return buildResponse(challenge);
    }

    // 현재 진행중인 챌린지 조회 (새로고침/재접속 시 상태 복원용)
    public ChallengeResponse getActiveChallenge(Long userId) {
        Challenge challenge = challengeRepository.findByUserIdAndStatus(userId, Challenge.Status.진행중)
                .orElseThrow(() -> new EntityNotFoundException("진행중인 챌린지가 없습니다."));
        return buildResponse(challenge);
    }

    // 냉장고 파먹기: 기간 중 새로 구매한 재료가 없어야 성공
    private boolean judgeFridgeClean(Challenge challenge) {
        boolean boughtDuringChallenge = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(challenge.getUserId(), UserIngredient.Status.보유중)
                .stream()
                .anyMatch(ui -> ui.getPurchaseDate() != null
                        && !ui.getPurchaseDate().isBefore(challenge.getStartDate())
                        && !ui.getPurchaseDate().isAfter(challenge.getEndDate()));
        return !boughtDuringChallenge;
    }

    // 특정 재료 소진: 선택한 재료들이 전부 "보유중" 상태로 안 남아있어야 성공
    private boolean judgeTargetIngredientConsumed(Challenge challenge) {
        return challenge.getTargetIngredients().stream()
                .allMatch(target -> userIngredientRepository
                        .findByUserIdAndIngredient_IngredientIdAndStatus(
                                challenge.getUserId(), target.getIngredientId(), UserIngredient.Status.보유중)
                        .isEmpty());
    }

    private ChallengeResponse buildResponse(Challenge challenge) {
        List<String> targetNames = challenge.getTargetIngredients().stream()
                .map(target -> ingredientRepository.findById(target.getIngredientId())
                        .map(Ingredient::getIngredientName)
                        .orElse("삭제된 재료"))
                .toList();
        return new ChallengeResponse(challenge, targetNames);
    }
}