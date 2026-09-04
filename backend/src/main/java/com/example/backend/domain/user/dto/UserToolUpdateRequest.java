package com.example.backend.domain.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

// 마이페이지에서 보유 조리도구를 다중선택해서 저장할 때 프론트가 보내는 데이터.
// 매번 선택된 전체 목록을 통째로 보내면, 서버는 기존 선택을 지우고 이 목록으로 다시 저장한다.
public record UserToolUpdateRequest(
        @NotNull List<Long> toolIds
) {
}
