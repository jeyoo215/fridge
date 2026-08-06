package com.example.backend.domain.review.dto;

import com.example.backend.domain.review.RecipeReview;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecipeReviewResponse {

    private final Long reviewId;
    private final Long userId;
    private final int rating;
    private final String content;
    private final LocalDateTime createdAt;

    public RecipeReviewResponse(RecipeReview entity) {
        this.reviewId = entity.getReviewId();
        this.userId = entity.getUserId();
        this.rating = entity.getRating();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
    }
}