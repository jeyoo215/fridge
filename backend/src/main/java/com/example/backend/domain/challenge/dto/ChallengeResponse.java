package com.example.backend.domain.challenge.dto;

import com.example.backend.domain.challenge.Challenge;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ChallengeResponse {

    private final Long challengeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String status;

    public ChallengeResponse(Challenge entity) {
        this.challengeId = entity.getChallengeId();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.status = entity.getStatus().name();
    }
}