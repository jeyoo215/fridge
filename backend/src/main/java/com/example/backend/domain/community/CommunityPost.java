package com.example.backend.domain.community;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 레시피를 소개하는 커뮤니티 게시글 (제목 + 재료 + 번호가 매겨진 조리순서로 구성. 각 조리순서 단계는
// 리치텍스트 본문 + 이미지/동영상 첨부를 가질 수 있어서, 예전의 "섹션" 역할까지 함께 담당한다).
// 재료/조리순서를 구조화해서 같이 받아두면, 좋아요가 많이 쌓였을 때 정식 Recipe로 승격시킬 수 있다.
@Entity
@Table(name = "community_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    // 레시피 카테고리/조리시간/난이도는 예전 글에는 없을 수 있어서 nullable로 두되,
    // 새 글은 CommunityPostCreateRequest의 @NotNull 검증으로 항상 채워지도록 강제한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private RecipeCategory category;

    @Column(name = "cooking_time_minutes")
    private Integer cookingTimeMinutes;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    // 게시판 구분. 예전 글(리팩터링 전)엔 값이 없을 수 있어 nullable로 두되, null은 RECIPE로 취급한다
    // (data.sql이 재시작 시 기존 글을 RECIPE로 백필하지만, 방어적으로 애플리케이션 레벨에서도 처리).
    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", length = 30)
    private BoardType boardType;

    // 전체 잡담 게시판(FREE_TALK) 전용 말머리. 그 외 게시판은 항상 null.
    // difficulty와 동일하게, 고정된 한글 옵션 목록이라 enum이 아니라 문자열 + 화이트리스트 검증 방식.
    @Column(name = "prefix", length = 20)
    private String prefix;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CommunityPostIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private final List<CommunityPostStep> steps = new ArrayList<>();

    // 좋아요(추천)가 임계치를 넘어 정식 레시피로 승격되면 채워짐. null이면 미승격 상태.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoted_recipe_id")
    private Recipe promotedRecipe;

    // 공감(좋아요) 개수. community_post_like row 수를 매번 COUNT하지 않도록 여기에 캐싱해서 들고 있는다
    // (좋아요 토글 시 CommunityPostLikeService가 increaseLikeCount/decreaseLikeCount로 갱신).
    @Column(name = "like_count", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int likeCount;

    // 조회수. 작성자 본인이 볼 때는 안 올라가고, 그 외엔(비로그인 포함) 상세를 열어볼 때마다 올라간다
    // (CommunityPostService.getDetail). 중복 방지(같은 사람 새로고침 등)는 하지 않는 단순 카운트.
    @Column(name = "view_count", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 신고가 임계치(CommunityReportService.HIDE_THRESHOLD) 이상 누적되면 자동으로 true가 되어
    // 목록/상세에서 안 보이게 된다. 관리자가 신고를 무시(기각)하면 다시 false로 풀린다.
    @Column(name = "hidden", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean hidden;

    @Builder
    public CommunityPost(Long userId, String title, RecipeCategory category,
                          Integer cookingTimeMinutes, String difficulty,
                          BoardType boardType, String prefix) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.boardType = boardType;
        this.prefix = prefix;
        this.createdAt = LocalDateTime.now();
    }

    public void addIngredient(Ingredient ingredient, BigDecimal quantity, String unit) {
        CommunityPostIngredient ingredientEntity = CommunityPostIngredient.builder()
                .ingredient(ingredient)
                .quantity(quantity)
                .unit(unit)
                .build();
        ingredientEntity.setPost(this);
        ingredients.add(ingredientEntity);
    }

    public void addStep(String description, String mediaUrl, CommunityPostStep.MediaType mediaType) {
        CommunityPostStep step = CommunityPostStep.builder()
                .stepOrder(steps.size())
                .description(description)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .build();
        step.setPost(this);
        steps.add(step);
    }

    // 게시글 수정: 제목/재료/조리순서를 통째로 새 내용으로 교체
    // (orphanRemoval=true라 컬렉션이 비워지는 순간 기존 값은 삭제됨)
    // boardType은 파라미터로 받지 않는다: 글 작성 후엔 게시판을 옮길 수 없다 (접근 제어 우회 방지).
    public void update(String title, RecipeCategory category, Integer cookingTimeMinutes,
                        String difficulty, String prefix) {
        if (isPromoted()) {
            throw new IllegalStateException("정식 레시피로 등록된 게시글은 수정할 수 없습니다.");
        }
        this.title = title;
        this.category = category;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.prefix = prefix;
        this.ingredients.clear();
        this.steps.clear();
    }

    public boolean isPromoted() {
        return promotedRecipe != null;
    }

    // 예전 글은 boardType이 null일 수 있어서, 그런 경우 RECIPE로 취급한다 (아직 있는 게시판은 그때 하나뿐이었음).
    public BoardType getEffectiveBoardType() {
        return boardType != null ? boardType : BoardType.RECIPE;
    }

    public boolean isChallengeBoard() {
        return getEffectiveBoardType() == BoardType.CHALLENGE_FRIDGE_CLEAN
                || getEffectiveBoardType() == BoardType.CHALLENGE_TARGET_INGREDIENT;
    }

    // 승격된 recipe row가 사라진(끊어진) 상태를 감지했을 때 복구용 (CommunityPostService/CommunityPostLikeService 전용).
    // 다시 null로 돌려놓으면 좋아요 임계치를 이미 넘긴 상태이므로 다음 토글 때 자연스럽게 재승격된다.
    public void clearPromotion() {
        this.promotedRecipe = null;
    }

    // 좋아요 토글 시 CommunityPostLikeService 전용
    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    // 상세 조회 시 CommunityPostService 전용
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 좋아요 임계치를 넘었을 때 호출 (CommunityPostPromotionService 전용)
    public void promote(Recipe recipe) {
        this.promotedRecipe = recipe;
    }

    // 신고 누적/관리자 처리 시 CommunityReportService 전용
    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    // 챌린지 게시판 2종류는 challenge 도메인의 ChallengeType과 1:1로 대응한다
    // (CommunityPostService.assertChallengeBoardAccess에서 매핑해서 사용).
    public enum BoardType {
        RECIPE,
        CHALLENGE_FRIDGE_CLEAN,
        CHALLENGE_TARGET_INGREDIENT,
        FREE_TALK
    }
}
