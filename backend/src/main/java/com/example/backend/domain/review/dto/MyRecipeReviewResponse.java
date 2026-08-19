package com.example.backend.domain.review.dto;

import com.example.backend.domain.review.RecipeReview;
import lombok.Getter;

import java.time.LocalDateTime;

// 마이페이지 "내가 평가한 레시피" 목록에 필요한 만큼만 담은 응답
@Getter
public class MyRecipeReviewResponse {

    private final Long recipeId;
    private final String recipeName;
    private final int rating;
    private final String content;
    private final LocalDateTime createdAt;

    public MyRecipeReviewResponse(RecipeReview review) {
        this.recipeId = review.getRecipe().getRecipeId();
        this.recipeName = review.getRecipe().getRecipeName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }
}
