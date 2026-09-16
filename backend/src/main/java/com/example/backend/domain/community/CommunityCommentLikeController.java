package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community/comments/{commentId}/likes")
@RequiredArgsConstructor
public class CommunityCommentLikeController {

    private final CommunityCommentLikeService communityCommentLikeService;

    // 누를 때마다 공감<->취소 토글
    @PostMapping
    public ToggleResponse toggle(@PathVariable("commentId") Long commentId,
                                  @AuthenticationPrincipal Long userId) {
        return communityCommentLikeService.toggle(userId, commentId);
    }
}
