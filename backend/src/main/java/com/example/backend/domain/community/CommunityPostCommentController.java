package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCommentCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostCommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommunityPostCommentController {

    private final CommunityPostCommentService communityPostCommentService;

    @PostMapping("/api/v1/community/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody CommunityPostCommentCreateRequest request) {
        Long commentId = communityPostCommentService.create(userId, postId, request);
        return Map.of("commentId", commentId);
    }

    // 등록순 조회 — 공용 조회, 토큰 불필요
    @GetMapping("/api/v1/community/posts/{postId}/comments")
    public List<CommunityPostCommentResponse> getComments(@PathVariable("postId") Long postId) {
        return communityPostCommentService.getComments(postId);
    }

    // 댓글 삭제 (본인 댓글만)
    @DeleteMapping("/api/v1/community/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal Long userId) {
        communityPostCommentService.delete(userId, commentId);
    }
}
