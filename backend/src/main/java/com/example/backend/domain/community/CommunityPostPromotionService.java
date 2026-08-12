package com.example.backend.domain.community;

import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.recipe.RecipeService;
import com.example.backend.domain.recipe.dto.RecipeCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

// 좋아요(추천)가 임계치를 넘은 커뮤니티 레시피 글을 정식 Recipe로 승격시킨다.
// 실제 저장은 RecipeService.createRecipe(이미 있던, 컨트롤러에 연결 안 된 로직)를 그대로 재사용한다.
@Service
@RequiredArgsConstructor
public class CommunityPostPromotionService {

    private static final String PROMOTED_RECIPE_SOURCE = "커뮤니티";

    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;

    @Transactional
    public void promote(CommunityPost post) {
        RecipeCreateRequest request = new RecipeCreateRequest(
                post.getCategory().getCategoryId(),
                post.getTitle(),
                post.getCookingTimeMinutes(),
                post.getDifficulty(),
                findThumbnailUrl(post),
                post.getIngredients().stream()
                        .map(item -> new RecipeCreateRequest.IngredientItem(
                                item.getIngredient().getIngredientId(), item.getQuantity(), item.getUnit()))
                        .toList(),
                post.getSteps().stream()
                        .sorted(Comparator.comparingInt(CommunityPostStep::getStepOrder))
                        .map(step -> new RecipeCreateRequest.StepItem(step.getStepOrder(), step.getDescription()))
                        .toList(),
                List.of(), // 커뮤니티 글쓰기 화면에서는 조리도구를 받지 않음
                PROMOTED_RECIPE_SOURCE
        );

        Long recipeId = recipeService.createRecipe(request);
        post.promote(recipeRepository.getReferenceById(recipeId));
    }

    // 목록 카드 썸네일과 동일한 규칙: 첫 섹션이 이미지일 때만 사용
    private String findThumbnailUrl(CommunityPost post) {
        return post.getSections().stream()
                .findFirst()
                .filter(section -> section.getMediaType() == CommunityPostSection.MediaType.IMAGE)
                .map(CommunityPostSection::getMediaUrl)
                .orElse(null);
    }
}
