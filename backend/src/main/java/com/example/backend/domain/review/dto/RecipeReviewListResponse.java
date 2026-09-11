package com.example.backend.domain.review.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class RecipeReviewListResponse {

    private final double averageRating;
    private final int reviewCount;
    private final List<RecipeReviewResponse> reviews;

    public RecipeReviewListResponse(double averageRating, List<RecipeReviewResponse> reviews) {
        this.averageRating = averageRating;
        this.reviewCount = reviews.size();
        this.reviews = reviews;
    }
}