package com.example.backend.domain.recipe;

import com.example.backend.domain.user.CookingTool;
import com.example.backend.domain.user.CookingToolRepository;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;

import com.example.backend.domain.recipe.dto.RecipeCategoryResponse;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import com.example.backend.domain.recipe.dto.RecipeDetailResponse;
import com.example.backend.domain.recipe.dto.RecipePageResponse;
import com.example.backend.domain.recipe.dto.RecipeSummaryResponse;

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
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final CookingToolRepository cookingToolRepository;

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

        List<RecipeSummaryResponse> content = recipeIds.stream()
                .map(recipesById::get)
                .filter(java.util.Objects::nonNull)
                .map(RecipeSummaryResponse::new)
                .toList();

        return new RecipePageResponse(content, page, idPage.getTotalPages(), idPage.getTotalElements());
    }
}