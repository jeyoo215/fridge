package com.example.backend.domain.review;

import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeCategory;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.review.dto.RecipeReviewCreateRequest;
import com.example.backend.domain.review.dto.RecipeReviewListResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeReviewServiceTest {

    @Mock private RecipeReviewRepository recipeReviewRepository;
    @Mock private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeReviewService recipeReviewService;

    @Test
    @DisplayName("존재하지 않는 레시피에는 후기를 등록할 수 없다")
    void createReview_존재하지않는레시피면_예외() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeReviewService.createReview(1L, 1L, new RecipeReviewCreateRequest(5, "맛있어요")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("정상적인 후기 등록 시 저장이 호출되고 id가 반환된다")
    void createReview_정상등록() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();
        Recipe recipe = Recipe.builder().category(category).recipeName("계란찜").build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        ArgumentCaptor<RecipeReview> captor = ArgumentCaptor.forClass(RecipeReview.class);
        RecipeReview saved = RecipeReview.builder().recipe(recipe).userId(1L).rating(5).content("맛있어요").build();
        ReflectionTestUtils.setField(saved, "reviewId", 100L);
        when(recipeReviewRepository.save(captor.capture())).thenReturn(saved);

        Long reviewId = recipeReviewService.createReview(1L, 1L, new RecipeReviewCreateRequest(5, "맛있어요"));

        assertThat(reviewId).isEqualTo(100L);
        assertThat(captor.getValue().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("후기가 없으면 평균 평점은 0.0이다")
    void getReviews_후기없으면_평균0() {
        when(recipeReviewRepository.findByRecipe_RecipeIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(recipeReviewRepository.findAverageRatingByRecipeId(1L)).thenReturn(null);

        RecipeReviewListResponse response = recipeReviewService.getReviews(1L);

        assertThat(response.getAverageRating()).isEqualTo(0.0);
        assertThat(response.getReviewCount()).isEqualTo(0);
    }
}