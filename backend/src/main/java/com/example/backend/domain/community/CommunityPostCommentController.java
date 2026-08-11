package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCommentCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostCommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommunityPostCommentController {

    private final CommunityPostCommentService communityPostCommentService;

    // 예: POST /api/v1/community/posts/1/comments?userId=1
    @PostMapping("/api/v1/community/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@PathVariable("postId") Long postId,
                                     @RequestParam("userId") Long userId,
                                     @Valid @RequestBody CommunityPostCommentCreateRequest request) {
        Long commentId = communityPostCommentService.create(userId, postId, request);
        return Map.of("commentId", commentId);
    }

    // 예: GET /api/v1/community/posts/1/comments (등록순)
    @GetMapping("/api/v1/community/posts/{postId}/comments")
    public List<CommunityPostCommentResponse> getComments(@PathVariable("postId") Long postId) {
        return communityPostCommentService.getComments(postId);
    }

    // 댓글 삭제 (본인 댓글만)
    // 예: DELETE /api/v1/community/comments/1?userId=1
    @DeleteMapping("/api/v1/community/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId, @RequestParam("userId") Long userId) {
        communityPostCommentService.delete(userId, commentId);
    }
}
