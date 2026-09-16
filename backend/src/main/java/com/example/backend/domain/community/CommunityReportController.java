package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityReportCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 일반 유저가 게시글/댓글을 신고하는 엔드포인트.
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityReportController {

    private final CommunityReportService communityReportService;

    // 예: POST /api/v1/community/posts/3/reports  { "reason": "스팸/광고" }
    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public void reportPost(@PathVariable("postId") Long postId,
                            @AuthenticationPrincipal Long userId,
                            @Valid @RequestBody CommunityReportCreateRequest request) {
        communityReportService.reportPost(userId, postId, request.reason());
    }

    // 예: POST /api/v1/community/comments/7/reports  { "reason": "욕설/혐오" }
    @PostMapping("/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public void reportComment(@PathVariable("commentId") Long commentId,
                               @AuthenticationPrincipal Long userId,
                               @Valid @RequestBody CommunityReportCreateRequest request) {
        communityReportService.reportComment(userId, commentId, request.reason());
    }
}
