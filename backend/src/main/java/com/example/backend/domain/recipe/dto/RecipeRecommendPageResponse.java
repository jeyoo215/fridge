package com.example.backend.domain.recipe.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class RecipeRecommendPageResponse {
    private final List<RecipeRecommendResponse> content;
    private final int page;
    private final int totalPages;
    private final long totalElements;

    public RecipeRecommendPageResponse(List<RecipeRecommendResponse> content, int page, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public static RecipeRecommendPageResponse empty(int page) {
        return new RecipeRecommendPageResponse(List.of(), page, 0, 0);
    }
}