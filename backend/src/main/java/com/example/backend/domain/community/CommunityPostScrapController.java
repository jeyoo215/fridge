package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community/posts/{postId}/scraps")
@RequiredArgsConstructor
public class CommunityPostScrapController {

    private final CommunityPostScrapService communityPostScrapService;

    // 예: POST /api/v1/community/posts/1/scraps?userId=1 (누를 때마다 스크랩<->취소 토글)
    @PostMapping
    public ToggleResponse toggle(@PathVariable("postId") Long postId,
                                  @RequestParam("userId") Long userId) {
        return communityPostScrapService.toggle(userId, postId);
    }

    // 예: GET /api/v1/community/posts/1/scraps?userId=1
    @GetMapping
    public ToggleResponse getStatus(@PathVariable("postId") Long postId,
                                     @RequestParam("userId") Long userId) {
        return communityPostScrapService.getStatus(userId, postId);
    }
}
