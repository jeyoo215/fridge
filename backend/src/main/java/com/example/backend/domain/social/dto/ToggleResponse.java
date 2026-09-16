package com.example.backend.domain.social.dto;

import lombok.Getter;

// 좋아요/스크랩 토글 후 응답 (지금 상태 + 전체 개수를 바로 알려줘서, 프론트가 다시 조회 안 해도 되게)
@Getter
public class ToggleResponse {

    private final boolean active; // 좋아요/스크랩 되어있는 상태인지
    private final long count;     // 그 레시피의 전체 좋아요(또는 스크랩) 개수

    public ToggleResponse(boolean active, long count) {
        this.active = active;
        this.count = count;
    }
}
