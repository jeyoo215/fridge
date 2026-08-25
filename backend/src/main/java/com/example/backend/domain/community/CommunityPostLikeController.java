package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community/posts/{postId}/likes")
@RequiredArgsConstructor
public class CommunityPostLikeController {

    private final CommunityPostLikeService communityPostLikeService;

    // 누를 때마다 좋아요<->취소 토글
    @PostMapping
    public ToggleResponse toggle(@PathVariable("postId") Long postId,
                                  @AuthenticationPrincipal Long userId) {
        return communityPostLikeService.toggle(userId, postId);
    }

    // 지금 좋아요 상태 + 총 개수
    @GetMapping
    public ToggleResponse getStatus(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal Long userId) {
        return communityPostLikeService.getStatus(userId, postId);
    }
}
