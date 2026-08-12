package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 마이페이지 "내 활동" 화면용 API
@RestController
@RequestMapping("/api/v1/users/me/community")
@RequiredArgsConstructor
public class CommunityActivityController {

    private final CommunityActivityService communityActivityService;

    // 예: GET /api/v1/users/me/community/scraps?userId=1
    @GetMapping("/scraps")
    public List<CommunityPostListResponse> getMyScraps(@RequestParam("userId") Long userId) {
        return communityActivityService.getMyScrappedPosts(userId);
    }

    // 예: GET /api/v1/users/me/community/likes?userId=1
    @GetMapping("/likes")
    public List<CommunityPostListResponse> getMyLikes(@RequestParam("userId") Long userId) {
        return communityActivityService.getMyLikedPosts(userId);
    }

    // 예: GET /api/v1/users/me/community/comments?userId=1
    @GetMapping("/comments")
    public List<CommunityPostListResponse> getMyComments(@RequestParam("userId") Long userId) {
        return communityActivityService.getMyCommentedPosts(userId);
    }
}
