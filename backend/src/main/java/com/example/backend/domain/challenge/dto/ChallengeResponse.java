package com.example.backend.domain.challenge.dto;

import com.example.backend.domain.challenge.Challenge;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ChallengeResponse {

    private final Long challengeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String status;
    private final String type;
    private final List<String> targetIngredientNames;

    public ChallengeResponse(Challenge entity, List<String> targetIngredientNames) {
        this.challengeId = entity.getChallengeId();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.status = entity.getStatus().name();
        this.type = entity.getType().name();
        this.targetIngredientNames = targetIngredientNames;
    }
}