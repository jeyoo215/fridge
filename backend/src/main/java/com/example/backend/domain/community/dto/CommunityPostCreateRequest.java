package com.example.backend.domain.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 커뮤니티 게시글 작성 요청. 제목 + 레시피 정보(카테고리/조리시간/난이도/재료/조리순서) 최소 1개 이상.
// 조리순서 각 단계는 리치텍스트 본문 + 이미지/동영상 첨부를 가질 수 있다 (예전의 "섹션" 역할까지 겸함).
// 좋아요가 쌓여 정식 레시피로 승격될 때 그대로 옮겨가므로 처음부터 구조화해서 받는다.
public record CommunityPostCreateRequest(
        @NotBlank String title,
        @NotNull Long categoryId,
        @NotNull Integer cookingTimeMinutes,
        @NotBlank String difficulty,
        @NotEmpty @Valid List<CommunityPostIngredientRequest> ingredients,
        @NotEmpty @Valid List<CommunityPostStepRequest> steps
) {
}
