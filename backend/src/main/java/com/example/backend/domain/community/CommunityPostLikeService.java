package com.example.backend.domain.community;

import com.example.backend.domain.recipe.RecipeRepository;
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
    private final RecipeRepository recipeRepository;

    // 좋아요 토글: 이미 눌렀으면 취소, 안 눌렀으면 새로 좋아요 (RecipeLikeService와 동일한 패턴).
    // 단, 정식 레시피로 이미 승격된 글은 추천을 취소할 수 없다.
    // 좋아요 개수는 매번 COUNT 쿼리로 세지 않고 CommunityPost.likeCount에 저장해서 관리한다.
    @Transactional
    public ToggleResponse toggle(Long userId, Long postId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        // 승격 표시는 있는데 실제 recipe row가 사라진 좀비 참조면 풀어준다 (data.sql 재시딩 버그 등에 대한 방어).
        // 좋아요 임계치는 이미 넘긴 상태이므로 여기서 풀어두면 이번 토글에서 바로 재승격된다.
        if (post.isPromoted() && !recipeRepository.existsById(post.getPromotedRecipe().getRecipeId())) {
            post.clearPromotion();
        }
        var existing = communityPostLikeRepository.findByPost_PostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            if (post.isPromoted()) {
                throw new IllegalArgumentException("이미 정식 레시피로 등록된 게시글은 추천을 취소할 수 없습니다.");
            }
            communityPostLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
        } else {
            communityPostLikeRepository.save(CommunityPostLike.builder().post(post).userId(userId).build());
            post.increaseLikeCount();

            // 승격은 레시피 게시판 글에만 적용된다. 챌린지/잡담 게시글은 카테고리/재료/조리순서가
            // 없어서 그대로 승격을 시도하면 CommunityPostPromotionService에서 NPE가 난다.
            boolean isRecipeBoard = post.getEffectiveBoardType() == CommunityPost.BoardType.RECIPE;
            if (isRecipeBoard && post.getLikeCount() >= PROMOTION_LIKE_THRESHOLD && !post.isPromoted()) {
                communityPostPromotionService.promote(post);
            }
        }

        boolean active = existing.isEmpty();
        return new ToggleResponse(active, post.getLikeCount());
    }

    // 지금 좋아요 상태 + 총 개수 조회 (게시글 상세 화면 진입 시 사용)
    public ToggleResponse getStatus(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        boolean active = communityPostLikeRepository.findByPost_PostIdAndUserId(postId, userId).isPresent();
        return new ToggleResponse(active, post.getLikeCount());
    }
}
