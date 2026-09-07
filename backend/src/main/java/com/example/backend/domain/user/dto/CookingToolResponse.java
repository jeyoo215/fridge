package com.example.backend.domain.user.dto;

import com.example.backend.domain.user.CookingTool;
import com.example.backend.domain.user.UserTool;
import lombok.Getter;

// 전체 조리도구 목록 조회, 내가 선택한 조리도구 목록 조회 양쪽에서 공통으로 씀 (모양이 같음)
@Getter
public class CookingToolResponse {

    private final Long toolId;
    private final String toolName;

    public CookingToolResponse(CookingTool entity) {
        this.toolId = entity.getToolId();
        this.toolName = entity.getToolName();
    }

    public CookingToolResponse(UserTool entity) {
        this.toolId = entity.getTool().getToolId();
        this.toolName = entity.getTool().getToolName();
    }
}
