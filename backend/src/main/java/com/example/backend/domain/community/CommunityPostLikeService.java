package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostLikeService {

    // 이 개수 이상 추천(좋아요)받으면 정식 레시피로 자동 승격된다.
    private static final long PROMOTION_LIKE_THRESHOLD = 10;

    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostPromotionService communityPostPromotionService;

    // 좋아요 토글: 이미 눌렀으면 취소, 안 눌렀으면 새로 좋아요 (RecipeLikeService와 동일한 패턴).
    // 단, 정식 레시피로 이미 승격된 글은 추천을 취소할 수 없다.
    @Transactional
    public ToggleResponse toggle(Long userId, Long postId) {
        var existing = communityPostLikeRepository.findByPost_PostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            if (existing.get().getPost().isPromoted()) {
                throw new IllegalArgumentException("이미 정식 레시피로 등록된 게시글은 추천을 취소할 수 없습니다.");
            }
            communityPostLikeRepository.delete(existing.get());
        } else {
            CommunityPost post = communityPostRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
            communityPostLikeRepository.save(CommunityPostLike.builder().post(post).userId(userId).build());

            long newCount = communityPostLikeRepository.countByPost_PostId(postId);
            if (newCount >= PROMOTION_LIKE_THRESHOLD && !post.isPromoted()) {
                communityPostPromotionService.promote(post);
            }
        }

        long count = communityPostLikeRepository.countByPost_PostId(postId);
        boolean active = existing.isEmpty();
        return new ToggleResponse(active, count);
    }

    // 지금 좋아요 상태 + 총 개수 조회 (게시글 상세 화면 진입 시 사용)
    public ToggleResponse getStatus(Long userId, Long postId) {
        boolean active = communityPostLikeRepository.findByPost_PostIdAndUserId(postId, userId).isPresent();
        long count = communityPostLikeRepository.countByPost_PostId(postId);
        return new ToggleResponse(active, count);
    }
}
