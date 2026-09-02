package com.example.backend.domain.community;

import com.example.backend.domain.challenge.Challenge;
import com.example.backend.domain.challenge.ChallengeRepository;
import com.example.backend.domain.community.dto.CommunityPostCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostDetailResponse;
import com.example.backend.domain.community.dto.CommunityPostIngredientRequest;
import com.example.backend.domain.community.dto.CommunityPostListResponse;
import com.example.backend.domain.community.dto.CommunityPostPageResponse;
import com.example.backend.domain.community.dto.CommunityPostStepRequest;
import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.recipe.RecipeCategory;
import com.example.backend.domain.recipe.RecipeCategoryRepository;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {

    // 전체 잡담 게시판(FREE_TALK) 글쓰기 시 반드시 하나를 골라야 하는 말머리 화이트리스트.
    static final List<String> FREE_TALK_PREFIXES = List.of("다이어터", "20대", "30대", "40대", "50대");

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostScrapRepository communityPostScrapRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final CommunityReportRepository communityReportRepository;

    // 탈퇴 등으로 작성자 계정이 이미 없는 경우의 표시용 대체 닉네임
    private static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";

    // 게시글/댓글 목록에서 매번 유저를 한 명씩 조회하지 않도록, userId 집합을 한 번에 조회해서
    // userId -> nickname 맵으로 만들어둔다.
    private Map<Long, String> findNicknamesByUserIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getNickname));
    }

    // 게시글 작성 (제목 + 재료/조리순서를 통째로 받아 한 번에 저장)
    @Transactional
    public Long create(Long userId, CommunityPostCreateRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        assertChallengeBoardAccess(userId, request.boardType());

        RecipeCategory category = null;
        if (request.boardType() == CommunityPost.BoardType.RECIPE) {
            assertRecipeFieldsPresent(request);
            category = findCategory(request.categoryId());
        } else if (request.boardType() == CommunityPost.BoardType.FREE_TALK) {
            assertValidPrefix(request.prefix());
        }

        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .title(request.title())
                .category(category)
                .cookingTimeMinutes(request.cookingTimeMinutes())
                .difficulty(request.difficulty())
                .boardType(request.boardType())
                .prefix(request.boardType() == CommunityPost.BoardType.FREE_TALK ? request.prefix() : null)
                .build();

        addIngredientsAndSteps(post, request);

        return communityPostRepository.save(post).getPostId();
    }

    private void assertRecipeFieldsPresent(CommunityPostCreateRequest request) {
        if (request.categoryId() == null || request.cookingTimeMinutes() == null
                || request.difficulty() == null || request.difficulty().isBlank()
                || request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new IllegalArgumentException("레시피 게시판 글은 카테고리/조리시간/난이도/재료를 모두 입력해야 합니다.");
        }
    }

    private void assertValidPrefix(String prefix) {
        if (prefix == null || !FREE_TALK_PREFIXES.contains(prefix)) {
            throw new IllegalArgumentException("말머리는 다음 중 하나를 선택해야 합니다: " + FREE_TALK_PREFIXES);
        }
    }

    // 챌린지 게시판은 "지금 그 종류의 챌린지를 진행 중인 유저"만 들어올 수 있다.
    // 진행중 챌린지는 유저당 최대 1개라는 규칙(ChallengeService.startChallenge)을 그대로 이용한다.
    private void assertChallengeBoardAccess(Long userId, CommunityPost.BoardType boardType) {
        Challenge.ChallengeType requiredType = toChallengeType(boardType);
        if (requiredType == null) {
            return;
        }
        if (userId == null) {
            throw new IllegalArgumentException("챌린지 게시판은 로그인한 유저만 접근할 수 있습니다.");
        }
        boolean matches = challengeRepository.findByUserIdAndStatus(userId, Challenge.Status.진행중)
                .map(challenge -> challenge.getType() == requiredType)
                .orElse(false);
        if (!matches) {
            throw new IllegalArgumentException("이 챌린지를 진행 중이어야 게시판에 들어갈 수 있습니다.");
        }
    }

    private Challenge.ChallengeType toChallengeType(CommunityPost.BoardType boardType) {
        return switch (boardType) {
            case CHALLENGE_FRIDGE_CLEAN -> Challenge.ChallengeType.FRIDGE_CLEAN;
            case CHALLENGE_TARGET_INGREDIENT -> Challenge.ChallengeType.TARGET_INGREDIENT;
            default -> null;
        };
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
            post.addStep(item.description(), item.mediaUrl(), item.mediaType());
        }
    }

    // 게시판 목록 (기본 최신순, sortBy="popular"면 좋아요 많은 순).
    // boardType이 챌린지 게시판이면 userId로 접근 자격을 먼저 확인한다. prefix는 FREE_TALK 게시판의
    // 말머리 필터(선택)로만 쓰인다. keyword가 있으면 제목 검색을 하고, 이때는 prefix 필터를 무시한다.
    @Transactional
    public CommunityPostPageResponse getList(int page, int size, String sortBy,
                                              CommunityPost.BoardType boardType, String prefix, String keyword, Long userId) {
        assertChallengeBoardAccess(userId, boardType);

        Pageable pageable = PageRequest.of(page, size);
        boolean popular = "popular".equals(sortBy);
        // "감자 주스"로 검색해도 "감자주스"가 나오도록, DB에 저장된 제목뿐 아니라 검색어 쪽 공백도 미리 지운다
        // (REPLACE(title, ' ', '')와 비교하므로 둘 다 공백 없는 형태로 맞춰야 함).
        String trimmedKeyword = keyword == null ? null : keyword.trim().replaceAll("\\s+", "");
        Page<Long> idPage;
        if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            idPage = popular
                    ? communityPostRepository.findPostIdsByBoardTypeAndTitleContainingOrderByLikeCountDesc(boardType, trimmedKeyword, pageable)
                    : communityPostRepository.findPostIdsByBoardTypeAndTitleContainingOrderByCreatedAtDesc(boardType, trimmedKeyword, pageable);
        } else if (boardType == CommunityPost.BoardType.FREE_TALK && prefix != null) {
            idPage = popular
                    ? communityPostRepository.findPostIdsByBoardTypeAndPrefixOrderByLikeCountDesc(boardType, prefix, pageable)
                    : communityPostRepository.findPostIdsByBoardTypeAndPrefixOrderByCreatedAtDesc(boardType, prefix, pageable);
        } else {
            idPage = popular
                    ? communityPostRepository.findPostIdsByBoardTypeOrderByLikeCountDesc(boardType, pageable)
                    : communityPostRepository.findPostIdsByBoardTypeOrderByCreatedAtDesc(boardType, pageable);
        }

        List<Long> postIds = idPage.getContent();
        if (postIds.isEmpty()) {
            return new CommunityPostPageResponse(List.of(), page, idPage.getTotalPages(), idPage.getTotalElements());
        }

        Map<Long, CommunityPost> postsById = communityPostRepository.findAllWithStepsByPostIdIn(postIds)
                .stream()
                .collect(Collectors.toMap(CommunityPost::getPostId, post -> post));
        postsById.values().forEach(this::repairDanglingPromotion);

        Map<Long, String> nicknamesByUserId = findNicknamesByUserIds(
                postsById.values().stream().map(CommunityPost::getUserId).collect(Collectors.toSet())
        );

        // id 목록의 정렬(최신순 또는 인기순)을 그대로 유지하기 위해 IN 조회 결과를 postIds 순서에 맞춰 다시 매핑한다.
        List<CommunityPostListResponse> content = postIds.stream()
                .map(postsById::get)
                .map(post -> new CommunityPostListResponse(
                        post, nicknamesByUserId.getOrDefault(post.getUserId(), UNKNOWN_NICKNAME)))
                .toList();

        return new CommunityPostPageResponse(content, page, idPage.getTotalPages(), idPage.getTotalElements());
    }

    // 게시글 상세. 챌린지 게시판 글이면(직링크로 우회 접근하는 것을 막기 위해) userId로 자격을 확인한다.
    @Transactional
    public CommunityPostDetailResponse getDetail(Long postId, Long userId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        // 신고 누적으로 숨김 처리된 글은 작성자 본인만 볼 수 있음 (관리자 판단이 나기 전까지)
        if (post.isHidden() && !post.getUserId().equals(userId)) {
            throw new IllegalStateException("신고가 접수되어 관리자 검토 중인 게시글입니다.");
        }
        assertChallengeBoardAccess(userId, post.getEffectiveBoardType());
        repairDanglingPromotion(post);
        String nickname = userRepository.findById(post.getUserId())
                .map(User::getNickname)
                .orElse(UNKNOWN_NICKNAME);
        return new CommunityPostDetailResponse(post, nickname);
    }

    // 승격 표시(promotedRecipe)는 있는데 실제 recipe row가 없는 좀비 참조를 감지해서 풀어준다.
    // data.sql처럼 FK 체크를 끄고 recipe를 지우는 개발용 스크립트가 실수로 승격된 레시피까지 지워버리면
    // 이 상태가 될 수 있다 (data.sql 자체도 승격된 recipe_id는 보존하도록 고쳤지만, 방어적으로 한 번 더 확인).
    // 여기서 풀어두면 좋아요 임계치는 이미 넘긴 상태라 다음 토글 때 자동으로 재승격된다.
    private void repairDanglingPromotion(CommunityPost post) {
        if (post.isPromoted() && !recipeRepository.existsById(post.getPromotedRecipe().getRecipeId())) {
            post.clearPromotion();
        }
    }

    // 게시글 수정 (본인 글만 가능, 정식 레시피로 승격된 글은 수정 불가, 제목/재료/조리순서 전체를
    // 새 내용으로 교체). 게시판(boardType)은 요청 값을 무시하고 기존 게시글의 것을 그대로 유지한다
    // (다른 게시판으로 바꿔치기해서 접근 제어를 우회하는 걸 막기 위함).
    @Transactional
    public void update(Long userId, Long postId, CommunityPostCreateRequest request) {
        CommunityPost post = findOwnedPost(userId, postId);
        CommunityPost.BoardType boardType = post.getEffectiveBoardType();

        RecipeCategory category = null;
        String prefix = null;
        if (boardType == CommunityPost.BoardType.RECIPE) {
            assertRecipeFieldsPresent(request);
            category = findCategory(request.categoryId());
        } else if (boardType == CommunityPost.BoardType.FREE_TALK) {
            assertValidPrefix(request.prefix());
            prefix = request.prefix();
        }

        post.update(request.title(), category, request.cookingTimeMinutes(), request.difficulty(), prefix);
        addIngredientsAndSteps(post, request);
    }

    // 게시글 삭제 (본인 글만 가능, 정식 레시피로 승격된 글은 삭제 불가).
    // 좋아요/댓글/스크랩은 CommunityPost와 JPA 연관관계로 묶여있지 않아 cascade 삭제가 안 되므로
    // (재료/조리순서와 달리 별도 서비스가 관리), FK 제약 위반을 피하려면 먼저 직접 지워야 한다.
    @Transactional
    public void delete(Long userId, Long postId) {
        CommunityPost post = findOwnedPost(userId, postId);
        deletePostInternal(post);
    }

    // 관리자 전용: 신고 처리로 게시글을 강제 삭제 (작성자 소유 여부와 무관, CommunityReportService에서 호출)
    @Transactional
    public void adminDelete(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
        deletePostInternal(post);
    }

    // 좋아요/댓글/스크랩과, 이 게시글(및 댓글들)에 쌓여있던 신고 row까지 전부 정리한 뒤 게시글을 지운다.
    private void deletePostInternal(CommunityPost post) {
        if (post.isPromoted()) {
            throw new IllegalStateException("정식 레시피로 등록된 게시글은 삭제할 수 없습니다.");
        }
        Long postId = post.getPostId();
        List<Long> commentIds = communityPostCommentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommunityPostComment::getCommentId)
                .toList();
        if (!commentIds.isEmpty()) {
            communityReportRepository.deleteByTargetTypeAndTargetIdIn(CommunityReport.TargetType.COMMENT, commentIds);
        }
        communityReportRepository.deleteByTargetTypeAndTargetId(CommunityReport.TargetType.POST, postId);
        communityPostLikeRepository.deleteByPost_PostId(postId);
        communityPostCommentRepository.deleteByPost_PostId(postId);
        communityPostScrapRepository.deleteByPost_PostId(postId);
        communityPostRepository.delete(post);
    }

    // 본인 소유의 게시글이 맞는지 확인 후 반환 (다른 사람 글을 못 건드리게 방지)
    private CommunityPost findOwnedPost(Long userId, Long postId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 글만 수정/삭제할 수 있습니다.");
        }
        return post;
    }
}
