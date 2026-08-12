package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPost;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CommunityPostDetailResponse {

    private final Long postId;
    private final Long userId;
    private final String title;
    private final LocalDateTime createdAt;
    private final long likeCount;
    private final List<CommunityPostSectionResponse> sections;

    public CommunityPostDetailResponse(CommunityPost entity, long likeCount) {
        this.postId = entity.getPostId();
        this.userId = entity.getUserId();
        this.title = entity.getTitle();
        this.createdAt = entity.getCreatedAt();
        this.likeCount = likeCount;
        this.sections = entity.getSections().stream()
                .map(CommunityPostSectionResponse::new)
                .toList();
    }
}
