package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeLikeService {

    private final RecipeLikeRepository recipeLikeRepository;
    private final RecipeRepository recipeRepository;

    // 좋아요 토글: 이미 눌렀으면 취소, 안 눌렀으면 새로 좋아요
    @Transactional
    public ToggleResponse toggle(Long userId, Long recipeId) {
        var existing = recipeLikeRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId);

        if (existing.isPresent()) {
            recipeLikeRepository.delete(existing.get());
        } else {
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다. id=" + recipeId));
            recipeLikeRepository.save(RecipeLike.builder().recipe(recipe).userId(userId).build());
        }

        long count = recipeLikeRepository.countByRecipe_RecipeId(recipeId);
        boolean active = existing.isEmpty(); // 방금 새로 눌렀으면 true, 취소했으면 false
        return new ToggleResponse(active, count);
    }

    // 지금 좋아요 상태 + 총 개수 조회 (레시피 상세 화면 진입 시 사용)
    public ToggleResponse getStatus(Long userId, Long recipeId) {
        boolean active = recipeLikeRepository.findByRecipe_RecipeIdAndUserId(recipeId, userId).isPresent();
        long count = recipeLikeRepository.countByRecipe_RecipeId(recipeId);
        return new ToggleResponse(active, count);
    }
}
