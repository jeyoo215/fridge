package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostDetailResponse;
import com.example.backend.domain.community.dto.CommunityPostPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody CommunityPostCreateRequest request) {
        return Map.of("postId", communityPostService.create(userId, request));
    }

    // 예: GET /api/v1/community/posts?page=0&size=10&sortBy=popular&boardType=FREE_TALK&prefix=20대
    // boardType 생략하면 레시피 게시판(RECIPE, 기존과 동일).
    // userId는 챌린지 게시판 접근 자격 확인용(선택) — 로그인 안 했으면 자동으로 null.
    // keyword는 제목 검색(선택)이며, 있으면 prefix 필터보다 우선한다.
    @GetMapping
    public CommunityPostPageResponse getList(@RequestParam(name = "page", defaultValue = "0") int page,
                                              @RequestParam(name = "size", defaultValue = "10") int size,
                                              @RequestParam(name = "sortBy", defaultValue = "latest") String sortBy,
                                              @RequestParam(name = "boardType", defaultValue = "RECIPE") CommunityPost.BoardType boardType,
                                              @RequestParam(name = "prefix", required = false) String prefix,
                                              @RequestParam(name = "keyword", required = false) String keyword,
                                              @AuthenticationPrincipal Long userId) {
        return communityPostService.getList(page, size, sortBy, boardType, prefix, keyword, userId);
    }

    // userId는 챌린지 게시판 글일 때 접근 자격 확인용(선택) — 로그인 안 했으면 자동으로 null.
    @GetMapping("/{postId}")
    public CommunityPostDetailResponse getDetail(@PathVariable("postId") Long postId,
                                                  @AuthenticationPrincipal Long userId) {
        return communityPostService.getDetail(postId, userId);
    }

    // 게시글 수정 (본인 글만). 제목/섹션을 통째로 새 내용으로 교체.
    @PutMapping("/{postId}")
    public void update(@PathVariable("postId") Long postId,
                        @AuthenticationPrincipal Long userId,
                        @Valid @RequestBody CommunityPostCreateRequest request) {
        communityPostService.update(userId, postId, request);
    }

    // 게시글 삭제 (본인 글만)
    @DeleteMapping("/{postId}")
    public void delete(@PathVariable("postId") Long postId, @AuthenticationPrincipal Long userId) {
        communityPostService.delete(userId, postId);
    }
}
