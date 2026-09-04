package com.example.backend.domain.community.dto;

import com.example.backend.domain.community.CommunityPost;
import com.example.backend.domain.community.CommunityPostStep;
import lombok.Getter;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

// 게시판 목록 화면 카드 하나. 첫 조리순서 단계의 이미지/본문에서 썸네일과 미리보기 텍스트를 뽑아 보여준다.
@Getter
public class CommunityPostListResponse {

    private final Long postId;
    private final Long userId;
    private final String nickname;
    private final String title;
    private final String thumbnailUrl;
    private final String previewText;
    private final LocalDateTime createdAt;
    private final long likeCount;
    private final long viewCount;
    private final Long promotedRecipeId;
    private final String boardType;
    private final String prefix;

    public CommunityPostListResponse(CommunityPost entity, String nickname) {
        this.postId = entity.getPostId();
        this.userId = entity.getUserId();
        this.nickname = nickname;
        this.title = entity.getTitle();
        this.createdAt = entity.getCreatedAt();
        this.likeCount = entity.getLikeCount();
        this.viewCount = entity.getViewCount();
        this.promotedRecipeId = entity.isPromoted() ? entity.getPromotedRecipe().getRecipeId() : null;
        this.boardType = entity.getEffectiveBoardType().name();
        this.prefix = entity.getPrefix();

        CommunityPostStep firstStep = entity.getSteps().isEmpty() ? null : entity.getSteps().get(0);
        // 목록 카드 썸네일은 이미지일 때만 보여준다 (동영상은 재생 없이 정지 프레임을 뽑을 방법이 없어서 생략).
        this.thumbnailUrl = firstStep != null && firstStep.getMediaType() == CommunityPostStep.MediaType.IMAGE
                ? firstStep.getMediaUrl()
                : null;
        this.previewText = firstStep != null ? toPreviewText(firstStep.getDescription()) : "";
    }

    // 목록 카드엔 HTML 서식 없이 순수 텍스트 미리보기만 필요해서 태그를 제거하고 길이를 제한한다.
    // 태그를 없애기 전에 &nbsp; 같은 HTML 엔티티부터 실제 문자로 풀어줘야 "&nbsp;" 글자가 그대로 안 남는다.
    // htmlUnescape가 만들어내는 줄바꿈없는공백( )은 일반 공백으로 바꿔서 자연스럽게 보이게 한다.
    private static String toPreviewText(String html) {
        if (html == null) {
            return "";
        }
        String unescaped = HtmlUtils.htmlUnescape(html).replace(' ', ' ');
        String plain = unescaped.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        return plain.length() > 100 ? plain.substring(0, 100) + "..." : plain;
    }
}
