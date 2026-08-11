package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPostSection;
import lombok.Getter;

@Getter
public class CommunityPostSectionResponse {

    private final String subtitle;
    private final String content;
    private final String mediaUrl;
    private final CommunityPostSection.MediaType mediaType;

    public CommunityPostSectionResponse(CommunityPostSection entity) {
        this.subtitle = entity.getSubtitle();
        this.content = entity.getContent();
        this.mediaUrl = entity.getMediaUrl();
        this.mediaType = entity.getMediaType();
    }
}
