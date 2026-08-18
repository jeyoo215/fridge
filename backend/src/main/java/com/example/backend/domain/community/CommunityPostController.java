package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostDetailResponse;
import com.example.backend.domain.community.dto.CommunityPostPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: POST /api/v1/community/posts?userId=1
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@RequestParam("userId") Long userId,
                                     @Valid @RequestBody CommunityPostCreateRequest request) {
        return Map.of("postId", communityPostService.create(userId, request));
    }

    // 예: GET /api/v1/community/posts?page=0&size=10&sortBy=popular&boardType=FREE_TALK&prefix=20대&userId=1
    // boardType 생략하면 레시피 게시판(RECIPE, 기존과 동일). userId는 챌린지 게시판 접근 자격 확인용(선택).
    @GetMapping
    public CommunityPostPageResponse getList(@RequestParam(name = "page", defaultValue = "0") int page,
                                              @RequestParam(name = "size", defaultValue = "10") int size,
                                              @RequestParam(name = "sortBy", defaultValue = "latest") String sortBy,
                                              @RequestParam(name = "boardType", defaultValue = "RECIPE") CommunityPost.BoardType boardType,
                                              @RequestParam(name = "prefix", required = false) String prefix,
                                              @RequestParam(name = "userId", required = false) Long userId) {
        return communityPostService.getList(page, size, sortBy, boardType, prefix, userId);
    }

    // 예: GET /api/v1/community/posts/1?userId=1 (userId는 챌린지 게시판 글일 때 접근 자격 확인용, 선택)
    @GetMapping("/{postId}")
    public CommunityPostDetailResponse getDetail(@PathVariable("postId") Long postId,
                                                  @RequestParam(name = "userId", required = false) Long userId) {
        return communityPostService.getDetail(postId, userId);
    }

    // 게시글 수정 (본인 글만). 제목/섹션을 통째로 새 내용으로 교체.
    // 예: PUT /api/v1/community/posts/1?userId=1
    @PutMapping("/{postId}")
    public void update(@PathVariable("postId") Long postId,
                        @RequestParam("userId") Long userId,
                        @Valid @RequestBody CommunityPostCreateRequest request) {
        communityPostService.update(userId, postId, request);
    }

    // 게시글 삭제 (본인 글만)
    // 예: DELETE /api/v1/community/posts/1?userId=1
    @DeleteMapping("/{postId}")
    public void delete(@PathVariable("postId") Long postId, @RequestParam("userId") Long userId) {
        communityPostService.delete(userId, postId);
    }
}
