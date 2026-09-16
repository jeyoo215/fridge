package com.example.backend.domain.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

// ?덉떆???깅줉 ?붿껌 DTO (FR-24, FR-22 議곕━?꾧뎄)
public record RecipeCreateRequest(
        Long categoryId,                  // ?덉떆??移댄뀒怨좊━ id
        String recipeName,                // ?덉떆???대쫫
        Integer cookingTimeMinutes,       // 議곕━ ?쒓컙(遺?
        String difficulty,                // ?쒖씠??
        String imageUrl,                  // ????대?吏 url
        List<IngredientItem> ingredients, // ?꾩슂 ?щ즺 紐⑸줉
        List<StepItem> steps,             // 議곕━ ?쒖꽌 紐⑸줉
        List<Long> toolIds,               // ?꾩슂 議곕━?꾧뎄 id 紐⑸줉 (FR-22, cooking_tool 李몄“)
        String source                     // 異쒖쿂 (?? "而ㅻ??덊떚" ??而ㅻ??덊떚 湲 ?밴꺽?쇰줈 ?앹꽦??寃쎌슦)
) {
    // ?щ즺 ??ぉ (?щ즺 id + ?섎웾 + ?⑥쐞)
    public record IngredientItem(
            Long ingredientId,
            BigDecimal quantity,
            String unit
    ) {
    }

    // 議곕━ ?④퀎 ??ぉ (?쒖꽌 + ?ㅻ챸 + ?좏깮???대?吏/?숈쁺??. mediaType? "IMAGE"/"VIDEO" 臾몄옄??
    // (CookingStep.MediaType.valueOf濡?蹂?? ???꾨찓??媛?enum ?섏〈???쇳븯?ㅺ퀬 臾몄옄?대줈 諛쏅뒗??
    public record StepItem(
            Integer stepOrder,
            String description,
            String mediaUrl,
            String mediaType
    ) {
    }
}
