package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostStep;
import lombok.Getter;

@Getter
public class CommunityPostStepResponse {

    private final int stepOrder;
    private final String description;

    public CommunityPostStepResponse(CommunityPostStep entity) {
        this.stepOrder = entity.getStepOrder();
        this.description = entity.getDescription();
    }
}
