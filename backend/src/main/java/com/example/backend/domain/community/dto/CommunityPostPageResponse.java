package com.example.backend.domain.community.dto;

import lombok.Getter;

import java.util.List;

// 커뮤니티 목록 페이지네이션 응답 (한 페이지 = 10개)
@Getter
public class CommunityPostPageResponse {

    private final List<CommunityPostListResponse> content;
    private final int page;
    private final int totalPages;
    private final long totalElements;

    public CommunityPostPageResponse(List<CommunityPostListResponse> content, int page, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
