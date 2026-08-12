package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostSection;

// 이미지/동영상 업로드 응답. url은 상대 경로라 프론트가 API 호출에 쓰는 것과 같은 호스트를 붙여 써야 한다.
public record CommunityMediaUploadResponse(
        String url,
        CommunityPostSection.MediaType mediaType
) {
}
