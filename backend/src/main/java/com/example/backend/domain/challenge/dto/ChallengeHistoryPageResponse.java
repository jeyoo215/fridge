package com.example.backend.domain.challenge.dto;

import lombok.Getter;

import java.util.List;

// 챌린지 히스토리 페이지네이션 응답 (커뮤니티 목록과 동일한 포맷)
@Getter
public class ChallengeHistoryPageResponse {

    private final List<ChallengeResponse> content;
    private final int page;
    private final int totalPages;
    private final long totalElements;

    public ChallengeHistoryPageResponse(List<ChallengeResponse> content, int page, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}