package com.example.backend.domain.review;

import com.example.backend.domain.review.dto.RecipeReviewCreateRequest;
import com.example.backend.domain.review.dto.RecipeReviewListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/reviews")
@RequiredArgsConstructor
public class RecipeReviewController {

    private final RecipeReviewService recipeReviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createReview(@PathVariable("recipeId") Long recipeId,
                              @AuthenticationPrincipal Long userId,
                              @Valid @RequestBody RecipeReviewCreateRequest request) {
        return recipeReviewService.createReview(userId, recipeId, request);
    }

    @GetMapping
    public RecipeReviewListResponse getReviews(@PathVariable("recipeId") Long recipeId) {
        return recipeReviewService.getReviews(recipeId);
    }
}
