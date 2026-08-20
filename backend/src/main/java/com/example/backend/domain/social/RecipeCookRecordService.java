package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.social.dto.MadeRecipeResponse;
import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeCookRecordService {

    private final RecipeCookRecordRepository recipeCookRecordRepository;
    private final RecipeRepository recipeRepository;

    // "만들었어요" 토글: 이미 기록해뒀으면 취소, 안 해뒀으면 새로 기록
    @Transactional
    public ToggleResponse toggle(Long userId, Long recipeId) {
        var existing = recipeCookRecordRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId);

        if (existing.isPresent()) {
            recipeCookRecordRepository.delete(existing.get());
        } else {
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
            recipeCookRecordRepository.save(RecipeCookRecord.builder().recipe(recipe).userId(userId).build());
        }

        long count = recipeCookRecordRepository.countByRecipe_RecipeId(recipeId);
        boolean active = existing.isEmpty();
        return new ToggleResponse(active, count);
    }

    public ToggleResponse getStatus(Long userId, Long recipeId) {
        boolean active = recipeCookRecordRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId).isPresent();
        long count = recipeCookRecordRepository.countByRecipe_RecipeId(recipeId);
        return new ToggleResponse(active, count);
    }

    // 마이페이지 "내가 만들어본 레시피" 목록
    public List<MadeRecipeResponse> getMyMadeRecipes(Long userId) {
        return recipeCookRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MadeRecipeResponse::new)
                .toList();
    }
}
