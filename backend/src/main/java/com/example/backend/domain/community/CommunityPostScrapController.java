package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community/posts/{postId}/scraps")
@RequiredArgsConstructor
public class CommunityPostScrapController {

    private final CommunityPostScrapService communityPostScrapService;

    // 누를 때마다 스크랩<->취소 토글
    @PostMapping
    public ToggleResponse toggle(@PathVariable("postId") Long postId,
                                  @AuthenticationPrincipal Long userId) {
        return communityPostScrapService.toggle(userId, postId);
    }

    @GetMapping
    public ToggleResponse getStatus(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal Long userId) {
        return communityPostScrapService.getStatus(userId, postId);
    }
}
