package com.example.backend.domain.recipe.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class RecipePageResponse {
    private final List<RecipeSummaryResponse> content;
    private final int page;
    private final int totalPages;
    private final long totalElements;

    public RecipePageResponse(List<RecipeSummaryResponse> content, int page, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}