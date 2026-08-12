package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostDetailResponse;
import com.example.backend.domain.community.dto.CommunityPostIngredientRequest;
import com.example.backend.domain.community.dto.CommunityPostListResponse;
import com.example.backend.domain.community.dto.CommunityPostPageResponse;
import com.example.backend.domain.community.dto.CommunityPostSectionRequest;
import com.example.backend.domain.community.dto.CommunityPostStepRequest;
import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.recipe.RecipeCategory;
import com.example.backend.domain.recipe.RecipeCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final IngredientRepository ingredientRepository;

    // 게시글 작성 (제목 + 섹션 + 재료/조리순서를 통째로 받아 한 번에 저장)
    @Transactional
    public Long create(Long userId, CommunityPostCreateRequest request) {
        RecipeCategory category = findCategory(request.categoryId());

        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .title(request.title())
                .category(category)
                .cookingTimeMinutes(request.cookingTimeMinutes())
                .difficulty(request.difficulty())
                .build();

        for (CommunityPostSectionRequest section : request.sections()) {
            post.addSection(section.subtitle(), section.content(), section.mediaUrl(), section.mediaType());
        }
        addIngredientsAndSteps(post, request);

        return communityPostRepository.save(post).getPostId();
    }

    private RecipeCategory findCategory(Long categoryId) {
        return recipeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리입니다. id=" + categoryId));
    }

    private void addIngredientsAndSteps(CommunityPost post, CommunityPostCreateRequest request) {
        for (CommunityPostIngredientRequest item : request.ingredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.ingredientId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 재료입니다. id=" + item.ingredientId()));
            post.addIngredient(ingredient, item.quantity(), item.unit());
        }
        for (CommunityPostStepRequest item : request.steps()) {
            post.addStep(item.description());
        }
    }

    // 게시판 목록 (기본 최신순, sortBy="popular"면 좋아요 많은 순)
    public CommunityPostPageResponse getList(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> idPage = "popular".equals(sortBy)
                ? communityPostRepository.findPostIdsOrderByLikeCountDesc(pageable)
                : communityPostRepository.findPostIdsOrderByCreatedAtDesc(pageable);

        List<Long> postIds = idPage.getContent();
        if (postIds.isEmpty()) {
            return new CommunityPostPageResponse(List.of(), page, idPage.getTotalPages(), idPage.getTotalElements());
        }

        Map<Long, CommunityPost> postsById = communityPostRepository.findAllWithSectionsByPostIdIn(postIds)
                .stream()
                .collect(Collectors.toMap(CommunityPost::getPostId, post -> post));

        // id 목록의 정렬(최신순 또는 인기순)을 그대로 유지하기 위해 IN 조회 결과를 postIds 순서에 맞춰 다시 매핑한다.
        List<CommunityPostListResponse> content = postIds.stream()
                .map(postsById::get)
                .map(post -> new CommunityPostListResponse(post, communityPostLikeRepository.countByPost_PostId(post.getPostId())))
                .toList();

        return new CommunityPostPageResponse(content, page, idPage.getTotalPages(), idPage.getTotalElements());
    }

    // 게시글 상세
    public CommunityPostDetailResponse getDetail(Long postId) {
        CommunityPost post = communityPostRepository.findByIdWithSections(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        long likeCount = communityPostLikeRepository.countByPost_PostId(postId);
        return new CommunityPostDetailResponse(post, likeCount);
    }

    // 게시글 수정 (본인 글만 가능, 정식 레시피로 승격된 글은 수정 불가, 제목/섹션/재료/조리순서 전체를
    // 새 내용으로 교체)
    @Transactional
    public void update(Long userId, Long postId, CommunityPostCreateRequest request) {
        CommunityPost post = findOwnedPost(userId, postId);
        RecipeCategory category = findCategory(request.categoryId());

        post.update(request.title(), category, request.cookingTimeMinutes(), request.difficulty());
        for (CommunityPostSectionRequest section : request.sections()) {
            post.addSection(section.subtitle(), section.content(), section.mediaUrl(), section.mediaType());
        }
        addIngredientsAndSteps(post, request);
    }

    // 게시글 삭제 (본인 글만 가능, 정식 레시피로 승격된 글은 삭제 불가)
    @Transactional
    public void delete(Long userId, Long postId) {
        CommunityPost post = findOwnedPost(userId, postId);
        if (post.isPromoted()) {
            throw new IllegalStateException("정식 레시피로 등록된 게시글은 삭제할 수 없습니다.");
        }
        communityPostRepository.delete(post);
    }

    // 본인 소유의 게시글이 맞는지 확인 후 반환 (다른 사람 글을 못 건드리게 방지)
    private CommunityPost findOwnedPost(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findByIdWithSections(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 글만 수정/삭제할 수 있습니다.");
        }
        return post;
    }
}
