package com.example.backend.domain.social.dto;

import com.example.backend.domain.social.RecipeCookRecord;
import lombok.Getter;

import java.time.LocalDateTime;

// 마이페이지 "내가 만들어본 레시피" 목록에 필요한 만큼만 담은 응답
@Getter
public class MadeRecipeResponse {

    private final Long recipeId;
    private final String recipeName;
    private final String imageUrl;
    private final LocalDateTime madeAt;

    public MadeRecipeResponse(RecipeCookRecord record) {
        this.recipeId = record.getRecipe().getRecipeId();
        this.recipeName = record.getRecipe().getRecipeName();
        this.imageUrl = record.getRecipe().getImageUrl();
        this.madeAt = record.getCreatedAt();
    }
}
