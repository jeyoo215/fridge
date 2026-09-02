package com.example.backend.domain.recipe;

import com.example.backend.domain.user.CookingTool;
import com.example.backend.domain.user.CookingToolRepository;
import com.example.backend.domain.user.UserToolRepository;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;

import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipePageResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendPageResponse;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import com.example.backend.domain.recipe.dto.RecipeSummaryResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    // 유통기한 가중치 기준 (FR-21)
    private static final int EXPIRY_WEIGHT_D1 = 3; // D-1 이하
    private static final int EXPIRY_WEIGHT_D3 = 2; // D-3 이하

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository; // 📌 새로 생성한 Repository 주입
    private final IngredientRepository ingredientRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final CookingToolRepository cookingToolRepository;
    private final UserToolRepository userToolRepository;

    // 레시피 등록 (FR-24)
    @Transactional
    public Long createRecipe(RecipeCreateRequest request) {
        RecipeCategory category = recipeCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리입니다. id=" + request.categoryId()));

        Recipe recipe = Recipe.builder()
                .category(category)
                .recipeName(request.recipeName())
                .cookingTimeMinutes(request.cookingTimeMinutes())
                .difficulty(request.difficulty())
                .imageUrl(request.imageUrl())
                .source(request.source())
                .build();

        // 재료 목록 연결
        for (RecipeCreateRequest.IngredientItem item : request.ingredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.ingredientId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 재료입니다. id=" + item.ingredientId()));
            recipe.addRecipeIngredient(RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(item.quantity())
                    .unit(item.unit())
                    .build());
        }

        // 조리 순서 연결
        for (RecipeCreateRequest.StepItem item : request.steps()) {
            recipe.addCookingStep(CookingStep.builder()
                    .stepOrder(item.stepOrder())
                    .description(item.description())
                    .mediaUrl(item.mediaUrl())
                    .mediaType(item.mediaType() != null ? CookingStep.MediaType.valueOf(item.mediaType()) : null)
                    .build());
        }

        // 조리도구 연결
        for (Long toolId : request.toolIds()) {
            CookingTool tool = cookingToolRepository.findById(toolId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 조리도구입니다. id=" + toolId));
            recipe.addRecipeTool(RecipeTool.builder()
                .tool(tool)
                .build());
        }

        return recipeRepository.save(recipe).getRecipeId();
    }


    // 보유 재료 기반 레시피 추천 (FR-20 + FR-21 유통기한 가중치), 페이징 지원
    public RecipeRecommendPageResponse recommendRecipes(Long userId, int page, int size) {
        List<UserIngredient> myIngredients = userIngredientRepository
                .findByUserIdAndStatusOrderByExpirationDateAsc(userId, UserIngredient.Status.보유중);

        List<Long> ingredientIds = myIngredients.stream()
                .map(userIngredient -> userIngredient.getIngredient().getIngredientId())
                .toList();

        if (ingredientIds.isEmpty()) {
                return RecipeRecommendPageResponse.empty(page);
        }

        Map<Long, Long> dDayByIngredientId = myIngredients.stream()
                .collect(Collectors.toMap(
                        userIngredient -> userIngredient.getIngredient().getIngredientId(),
                        userIngredient -> ChronoUnit.DAYS.between(LocalDate.now(), userIngredient.getExpirationDate()),
                        (existing, duplicate) -> existing
                ));

        // 보유 재료(조미료 제외)와 겹치는 비조미료 재료 개수
        List<RecipeRepository.RecipeMatchResult> matchResults =
                recipeRepository.findRecipesByMatchingNonSeasoningIngredients(ingredientIds);

        if (matchResults.isEmpty()) {
                return RecipeRecommendPageResponse.empty(page);
        }

        Map<Long, Long> matchCountByRecipeId = matchResults.stream()
                .collect(Collectors.toMap(
                        RecipeRepository.RecipeMatchResult::getRecipeId,
                        RecipeRepository.RecipeMatchResult::getMatchCount
                ));

        // 레시피별 필요한 비조미료 재료 총 개수
        Map<Long, Long> totalRequiredByRecipeId = recipeRepository.findNonSeasoningIngredientCountPerRecipe().stream()
                .collect(Collectors.toMap(
                        RecipeRepository.RecipeMatchResult::getRecipeId,
                        RecipeRepository.RecipeMatchResult::getMatchCount
                ));

        // 필수조건: 필요한 비조미료 재료를 하나도 빠짐없이 다 갖고 있어야 함 (matchCount == totalRequired)
        // HashMap(matchCountByRecipeId)의 entrySet 순서는 보장되지 않으므로, 원래 쿼리 순서를 유지하는
        // matchResults 리스트를 기준으로 필터링한다.
        List<Long> recipeIds = matchResults.stream()
                .map(RecipeRepository.RecipeMatchResult::getRecipeId)
                .filter(recipeId -> matchCountByRecipeId.get(recipeId).equals(totalRequiredByRecipeId.get(recipeId)))
                .toList();

        if (recipeIds.isEmpty()) {
                return RecipeRecommendPageResponse.empty(page);
        }

        Set<Long> ownedToolIds = userToolRepository.findByUserId(userId).stream()
                .map(userTool -> userTool.getTool().getToolId())
                .collect(Collectors.toSet());

        Map<Long, Set<Long>> requiredToolIdsByRecipeId = recipeRepository.findToolIdPairsByRecipeIdIn(recipeIds).stream()
                .collect(Collectors.groupingBy(
                        RecipeRepository.RecipeToolIdPair::getRecipeId,
                        Collectors.mapping(RecipeRepository.RecipeToolIdPair::getToolId, Collectors.toSet())
                ));

        List<Recipe> recipes = recipeRepository.findAllWithIngredientsByRecipeIdIn(recipeIds);

        // 완전매칭된 후보만 남았으니, 이제부턴 유통기한 임박 재료 활용도로만 순위를 매긴다 (요청사항 2번)
        List<RecipeRecommendResponse> ranked = recipes.stream()
                .map(recipe -> {
                        long matchCount = matchCountByRecipeId.get(recipe.getRecipeId());
                        int expiryPriorityScore = calculateExpiryPriorityScore(recipe, dDayByIngredientId);
                        Set<Long> requiredToolIds = requiredToolIdsByRecipeId.getOrDefault(recipe.getRecipeId(), Set.of());
                        boolean hasAllTools = ownedToolIds.containsAll(requiredToolIds);
                        return new RecipeRecommendResponse(recipe, matchCount, expiryPriorityScore, hasAllTools);
                })
                .sorted(
                        Comparator
                                .comparingInt(RecipeRecommendResponse::getExpiryPriorityScore).reversed()
                                .thenComparing(Comparator.comparing(RecipeRecommendResponse::isHasAllTools).reversed())
                )
                .toList();

        int totalElements = ranked.size();
        int totalPages = (int) Math.ceil(totalElements / (double) size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        return new RecipeRecommendPageResponse(ranked.subList(fromIndex, toIndex), page, totalPages, totalElements);
        }

    // 레시피가 사용하는 재료 중, 보유 재료와 겹치는 것들의 유통기한 가중치 합산
    private int calculateExpiryPriorityScore(Recipe recipe, Map<Long, Long> dDayByIngredientId) {
        return recipe.getRecipeIngredients().stream()
                .mapToInt(recipeIngredient -> {
                    Long dDay = dDayByIngredientId.get(recipeIngredient.getIngredient().getIngredientId());
                    if (dDay == null) return 0;
                    if (dDay <= 1) return EXPIRY_WEIGHT_D1;
                    if (dDay <= 3) return EXPIRY_WEIGHT_D3;
                    return 0;
                })
                .sum();
    }

    // 레시피가 요구하는 조리도구를 사용자가 전부 보유하고 있는지 확인 (필요 도구 없는 레시피는 항상 통과) (FR-22)
    private boolean hasAllRequiredTools(Recipe recipe, Set<Long> ownedToolIds) {
        return recipe.getRecipeTools().stream()
                .map(recipeTool -> recipeTool.getTool().getToolId())
                .allMatch(ownedToolIds::contains);
    }

    // 레시피 카테고리 전체 목록 (커뮤니티 글쓰기 화면 드롭다운용)
    public List<RecipeCategoryResponse> getCategories() {
        return recipeCategoryRepository.findAll().stream()
                .map(RecipeCategoryResponse::new)
                .toList();
    }

    // 레시피 상세 조회 (FR-24)
    public RecipeDetailResponse getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
        return new RecipeDetailResponse(recipe);
    }

    // 레시피 목록/검색 (페이징 + 이름검색 + 재료필터)
    public RecipePageResponse getList(String keyword, List<Long> ingredientIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasIngredients = ingredientIds != null && !ingredientIds.isEmpty();

        // "감자 주스"로 검색해도 "감자주스"가 나오도록, DB에 저장된 이름뿐 아니라 검색어 쪽 공백도 미리 지운다
        // (REPLACE(recipeName, ' ', '')와 비교하므로 둘 다 공백 없는 형태로 맞춰야 함).
        String normalizedKeyword = hasKeyword ? keyword.trim().replaceAll("\\s+", "") : null;

        Page<Long> idPage;
        if (hasKeyword && hasIngredients) {
                idPage = recipeRepository.findRecipeIdsByNameAndIngredientIds(normalizedKeyword, ingredientIds, pageable);
        } else if (hasKeyword) {
                idPage = recipeRepository.findRecipeIdsByNameContaining(normalizedKeyword, pageable);
        } else if (hasIngredients) {
                idPage = recipeRepository.findRecipeIdsByIngredientIds(ingredientIds, pageable);
        } else {
                idPage = recipeRepository.findAllRecipeIds(pageable);
        }

        List<Long> recipeIds = idPage.getContent();
        if (recipeIds.isEmpty()) {
                return new RecipePageResponse(List.of(), page, idPage.getTotalPages(), idPage.getTotalElements());
        }

        Map<Long, Recipe> recipesById = recipeRepository.findAllById(recipeIds).stream()
                .collect(Collectors.toMap(Recipe::getRecipeId, r -> r));

        // idPage 순서(정렬 기준)를 그대로 유지하기 위해 recipeIds 순서대로 다시 매핑
        List<RecipeSummaryResponse> content = recipeIds.stream()
                .map(recipesById::get)
                .filter(java.util.Objects::nonNull)
                .map(RecipeSummaryResponse::new)
                .toList();

        return new RecipePageResponse(content, page, idPage.getTotalPages(), idPage.getTotalElements());
    }
}