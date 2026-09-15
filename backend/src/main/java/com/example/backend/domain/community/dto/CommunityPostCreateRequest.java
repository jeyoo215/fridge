package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPost;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 커뮤니티 게시글 작성 요청. 게시판(boardType)에 따라 나머지 필드의 필수 여부가 갈린다:
// - RECIPE: categoryId/cookingTimeMinutes/difficulty/ingredients 모두 필수 (승격 시 그대로 옮겨가므로).
// - FREE_TALK: prefix 필수 (다이어터/20대/30대/40대/50대 중 하나), 나머지는 비워둠.
// - CHALLENGE_*: 추가 필수값 없음.
// steps(조리순서/본문)는 게시판과 무관하게 최소 1개 필요 — 모든 게시판에서 리치텍스트 본문 블록 역할을 겸한다.
// boardType별 조건부 필수 검사는 어노테이션이 아니라 CommunityPostService에서 수행한다.
public record CommunityPostCreateRequest(
        @NotBlank String title,
        @NotNull CommunityPost.BoardType boardType,
        String prefix,
        Long categoryId,
        Integer cookingTimeMinutes,
        String difficulty,
        @Valid List<CommunityPostIngredientRequest> ingredients,
        @NotEmpty @Valid List<CommunityPostStepRequest> steps
) {
}
