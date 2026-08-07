package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.review.RecipeReviewRepository;
import com.example.backend.domain.social.dto.PopularRecipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularRecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeLikeRepository recipeLikeRepository;
    private final RecipeReviewRepository recipeReviewRepository; // review 도메인 것 그대로 읽기만 함 (수정 없음)

    // sortBy: "likes"(기본) 또는 "reviews"
    // TODO: 레시피가 아주 많아지면 이 방식(전체 조회 후 하나씩 개수 세기)은 느려짐.
    //       그땐 DB 쪽에서 좋아요/리뷰 개수를 미리 집계해두는 방식으로 바꿔야 함.
    public List<PopularRecipeResponse> getPopularRecipes(String sortBy) {
        List<Recipe> allRecipes = recipeRepository.findAll();

        List<PopularRecipeResponse> responses = allRecipes.stream()
                .map(recipe -> {
                    long likeCount = recipeLikeRepository.countByRecipe_RecipeId(recipe.getRecipeId());
                    long reviewCount = recipeReviewRepository
                            .findByRecipe_RecipeIdOrderByCreatedAtDesc(recipe.getRecipeId())
                            .size();
                    return new PopularRecipeResponse(recipe, likeCount, reviewCount);
                })
                .toList();

        Comparator<PopularRecipeResponse> comparator = "reviews".equals(sortBy)
                ? Comparator.comparingLong(PopularRecipeResponse::getReviewCount).reversed()
                : Comparator.comparingLong(PopularRecipeResponse::getLikeCount).reversed();

        return responses.stream().sorted(comparator).toList();
    }
}
