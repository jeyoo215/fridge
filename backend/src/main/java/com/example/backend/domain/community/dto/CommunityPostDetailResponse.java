package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPost;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class CommunityPostDetailResponse {

    private final Long postId;
    private final Long userId;
    private final String title;
    private final String categoryName;
    private final Integer cookingTimeMinutes;
    private final String difficulty;
    private final LocalDateTime createdAt;
    private final long likeCount;
    private final Long promotedRecipeId;
    private final String boardType;
    private final String prefix;
    private final List<CommunityPostIngredientResponse> ingredients;
    private final List<CommunityPostStepResponse> steps;

    public CommunityPostDetailResponse(CommunityPost entity) {
        this.postId = entity.getPostId();
        this.userId = entity.getUserId();
        this.title = entity.getTitle();
        this.categoryName = entity.getCategory() != null ? entity.getCategory().getCategoryName() : null;
        this.cookingTimeMinutes = entity.getCookingTimeMinutes();
        this.difficulty = entity.getDifficulty();
        this.createdAt = entity.getCreatedAt();
        this.likeCount = entity.getLikeCount();
        this.promotedRecipeId = entity.isPromoted() ? entity.getPromotedRecipe().getRecipeId() : null;
        this.boardType = entity.getEffectiveBoardType().name();
        this.prefix = entity.getPrefix();
        this.ingredients = entity.getIngredients().stream()
                .map(CommunityPostIngredientResponse::new)
                .toList();
        this.steps = entity.getSteps().stream()
                .sorted(Comparator.comparingInt(step -> step.getStepOrder()))
                .map(CommunityPostStepResponse::new)
                .toList();
    }
}
