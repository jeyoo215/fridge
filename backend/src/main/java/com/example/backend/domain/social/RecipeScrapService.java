package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.social.dto.ScrapedRecipeResponse;
import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeScrapService {

    private final RecipeScrapRepository recipeScrapRepository;
    private final RecipeRepository recipeRepository;

    // 스크랩 토글: 이미 해뒀으면 취소, 안 해뒀으면 새로 스크랩
    @Transactional
    public ToggleResponse toggle(Long userId, Long recipeId) {
        var existing = recipeScrapRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId);

        if (existing.isPresent()) {
            recipeScrapRepository.delete(existing.get());
        } else {
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
            recipeScrapRepository.save(RecipeScrap.builder().recipe(recipe).userId(userId).build());
        }

        long count = recipeScrapRepository.countByRecipe_RecipeId(recipeId);
        boolean active = existing.isEmpty();
        return new ToggleResponse(active, count);
    }

    public ToggleResponse getStatus(Long userId, Long recipeId) {
        boolean active = recipeScrapRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId).isPresent();
        long count = recipeScrapRepository.countByRecipe_RecipeId(recipeId);
        return new ToggleResponse(active, count);
    }

    // 마이페이지 "내가 스크랩한 레시피" 목록
    public List<ScrapedRecipeResponse> getMyScraps(Long userId) {
        return recipeScrapRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ScrapedRecipeResponse::new)
                .toList();
    }
}
