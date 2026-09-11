package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostStep;
import jakarta.validation.constraints.NotBlank;

// 조리순서 한 단계. description은 리치텍스트 에디터가 만든 HTML이고, 이미지/동영상은 선택 첨부.
// mediaUrl은 미리 CommunityMediaController로 업로드해서 받은 값을 그대로 넣는다.
public record CommunityPostStepRequest(
        @NotBlank String description,
        String mediaUrl,
        CommunityPostStep.MediaType mediaType
) {
}
