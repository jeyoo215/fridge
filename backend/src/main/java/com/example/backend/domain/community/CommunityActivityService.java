package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 마이페이지 "내 활동": 내가 스크랩/좋아요/댓글단 게시글 목록
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityActivityService {

    private final CommunityPostScrapRepository communityPostScrapRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostRepository communityPostRepository;

    public List<CommunityPostListResponse> getMyScrappedPosts(Long userId) {
        List<Long> postIds = communityPostScrapRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(scrap -> scrap.getPost().getPostId())
                .toList();
        return toListResponses(postIds);
    }

    public List<CommunityPostListResponse> getMyLikedPosts(Long userId) {
        List<Long> postIds = communityPostLikeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(like -> like.getPost().getPostId())
                .toList();
        return toListResponses(postIds);
    }

    public List<CommunityPostListResponse> getMyCommentedPosts(Long userId) {
        // 같은 글에 댓글을 여러 번 달았을 수 있으니, 가장 최근 댓글 기준으로 글 하나만 남긴다.
        List<Long> postIds = communityPostCommentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(comment -> comment.getPost().getPostId())
                .distinct()
                .toList();
        return toListResponses(postIds);
    }

    // postId 목록(정렬 순서 있음)을 받아 섹션까지 채운 CommunityPostListResponse로 변환
    private List<CommunityPostListResponse> toListResponses(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return List.of();
        }

        Map<Long, CommunityPost> postsById = communityPostRepository.findAllWithStepsByPostIdIn(postIds)
                .stream()
                .collect(Collectors.toMap(CommunityPost::getPostId, post -> post, (a, b) -> a, LinkedHashMap::new));

        return postIds.stream()
                .map(postsById::get)
                .filter(post -> post != null) // 스크랩/좋아요/댓글 이후 삭제된 글은 건너뜀
                .map(CommunityPostListResponse::new)
                .toList();
    }
}
