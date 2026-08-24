package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 마이페이지 "내 활동" 화면용 API
@RestController
@RequestMapping("/api/v1/users/me/community")
@RequiredArgsConstructor
public class CommunityActivityController {

    private final CommunityActivityService communityActivityService;

    @GetMapping("/scraps")
    public List<CommunityPostListResponse> getMyScraps(@AuthenticationPrincipal Long userId) {
        return communityActivityService.getMyScrappedPosts(userId);
    }

    @GetMapping("/likes")
    public List<CommunityPostListResponse> getMyLikes(@AuthenticationPrincipal Long userId) {
        return communityActivityService.getMyLikedPosts(userId);
    }

    @GetMapping("/comments")
    public List<CommunityPostListResponse> getMyComments(@AuthenticationPrincipal Long userId) {
        return communityActivityService.getMyCommentedPosts(userId);
    }
}
