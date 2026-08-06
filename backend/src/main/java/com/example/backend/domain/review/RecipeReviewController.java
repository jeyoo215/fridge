package com.example.backend.domain.review;

import com.example.backend.domain.review.dto.RecipeReviewCreateRequest;
import com.example.backend.domain.review.dto.RecipeReviewListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/reviews")
@RequiredArgsConstructor
public class RecipeReviewController {

    private final RecipeReviewService recipeReviewService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createReview(@PathVariable Long recipeId,
                              @RequestParam Long userId,
                              @Valid @RequestBody RecipeReviewCreateRequest request) {
        return recipeReviewService.createReview(userId, recipeId, request);
    }

    @GetMapping
    public RecipeReviewListResponse getReviews(@PathVariable Long recipeId) {
        return recipeReviewService.getReviews(recipeId);
    }
}