package com.example.backend.domain.review;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.review.dto.RecipeReviewCreateRequest;
import com.example.backend.domain.review.dto.RecipeReviewListResponse;
import com.example.backend.domain.review.dto.RecipeReviewResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeReviewService {

    private final RecipeReviewRepository recipeReviewRepository;
    private final RecipeRepository recipeRepository;

    // 후기/평점 등록 (FR-41)
    @Transactional
    public Long createReview(Long userId, Long recipeId, RecipeReviewCreateRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));

        RecipeReview review = RecipeReview.builder()
                .recipe(recipe)
                .userId(userId)
                .rating(request.rating())
                .content(request.content())
                .build();

        return recipeReviewRepository.save(review).getReviewId();
    }

    // 레시피의 후기 목록 + 평균 평점 조회
    public RecipeReviewListResponse getReviews(Long recipeId) {
        var reviews = recipeReviewRepository.findByRecipe_RecipeIdOrderByCreatedAtDesc(recipeId).stream()
                .map(RecipeReviewResponse::new)
                .toList();

        Double average = recipeReviewRepository.findAverageRatingByRecipeId(recipeId);

        return new RecipeReviewListResponse(average != null ? average : 0.0, reviews);
    }
}