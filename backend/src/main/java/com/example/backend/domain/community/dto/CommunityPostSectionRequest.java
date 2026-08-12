package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostSection;
import jakarta.validation.constraints.NotBlank;

// 게시글 작성 화면에서 섹션(소제목+본문+미디어) 하나. content는 리치텍스트 에디터가 만든 HTML.
// mediaUrl/mediaType은 미리 CommunityMediaController로 업로드해서 받은 값을 그대로 넣는다.
public record CommunityPostSectionRequest(
        @NotBlank String subtitle,
        @NotBlank String content,
        String mediaUrl,
        CommunityPostSection.MediaType mediaType
) {
}
