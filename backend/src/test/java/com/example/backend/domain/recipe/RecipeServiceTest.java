package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.RecipeRecommendResponse;
import com.example.backend.domain.user.CookingTool;
import com.example.backend.domain.user.CookingToolRepository;
import com.example.backend.domain.user.UserTool;
import com.example.backend.domain.user.UserToolRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeCategoryRepository recipeCategoryRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private UserIngredientRepository userIngredientRepository;
    @Mock private CookingToolRepository cookingToolRepository;
    @Mock private UserToolRepository userToolRepository; 

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("보유 재료가 없으면 추천 목록은 빈 리스트다")
    void recommendRecipes_보유재료없으면_빈리스트() {
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of());

        List<RecipeRecommendResponse> result = recipeService.recommendRecipes(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유통기한이 D-1인 재료를 쓰는 레시피가, 매칭 개수는 적어도 더 높은 순위로 온다 (FR-21)")
    void recommendRecipes_유통기한임박재료_우선순위() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        Ingredient onion = Ingredient.builder().ingredientName("양파").isSeasoning(false).build();
        Ingredient garlic = Ingredient.builder().ingredientName("마늘").isSeasoning(false).build();
        setIngredientId(egg, 1L);
        setIngredientId(onion, 2L);
        setIngredientId(garlic, 3L);

        // 레시피 A: 계란만 씀 (D-1 임박 재료 활용, 가중치 3점) → matchCount 1개
        Recipe recipeA = Recipe.builder().category(category).recipeName("계란요리").build();
        recipeA.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(java.math.BigDecimal.ONE).unit("개").build());
        setRecipeId(recipeA, 10L);

        // 레시피 B: 양파+마늘만 씀 (임박 재료 하나도 안 씀, 가중치 0점) → matchCount 2개로 더 많음
        Recipe recipeB = Recipe.builder().category(category).recipeName("양파마늘볶음").build();
        recipeB.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(java.math.BigDecimal.ONE).unit("개").build());
        recipeB.addRecipeIngredient(RecipeIngredient.builder().ingredient(garlic).quantity(java.math.BigDecimal.ONE).unit("개").build());
        setRecipeId(recipeB, 20L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(java.math.BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(1)) // D-1
                .build();
        UserIngredient myOnion = UserIngredient.builder()
                .userId(1L).ingredient(onion).quantity(java.math.BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박
                .build();
        UserIngredient myGarlic = UserIngredient.builder()
                .userId(1L).ingredient(garlic).quantity(java.math.BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg, myOnion, myGarlic));
        when(recipeRepository.findRecipesByMatchingIngredients(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(
                        matchResult(10L, 1L),
                        matchResult(20L, 2L)
                ));
        when(recipeRepository.findAllById(List.of(10L, 20L)))
                .thenReturn(List.of(recipeA, recipeB));

        List<RecipeRecommendResponse> result = recipeService.recommendRecipes(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipeId()).isEqualTo(10L); // 매칭 1개뿐이지만 1순위여야 함
        assertThat(result.get(0).getExpiryPriorityScore()).isEqualTo(3);
        assertThat(result.get(1).getRecipeId()).isEqualTo(20L);
        assertThat(result.get(1).getExpiryPriorityScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 레시피 상세 조회 시 예외가 발생한다")
    void getRecipeDetail_존재하지않으면_예외() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeDetail(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private RecipeRepository.RecipeMatchResult matchResult(Long recipeId, Long matchCount) {
        return new RecipeRepository.RecipeMatchResult() {
            public Long getRecipeId() { return recipeId; }
            public Long getMatchCount() { return matchCount; }
        };
    }

    // 테스트에서만 필요한 id 세팅 (엔티티 id는 @GeneratedValue라 리플렉션으로 강제 주입)
    private void setIngredientId(Ingredient ingredient, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(ingredient, "ingredientId", id);
    }

    private void setRecipeId(Recipe recipe, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(recipe, "recipeId", id);
    }


    @Test
    @DisplayName("매칭 개수/유통기한 점수가 같아도, 보유한 조리도구로 만들 수 있는 레시피가 우선순위가 높다 (FR-22)")
        void recommendRecipes_보유조리도구_우선순위() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient tofu = Ingredient.builder().ingredientName("두부").isSeasoning(false).build();
        setIngredientId(tofu, 4L);

        CookingTool pan = CookingTool.builder().toolName("프라이팬").build();
        setToolId(pan, 1L);
        CookingTool oven = CookingTool.builder().toolName("오븐").build();
        setToolId(oven, 2L);

        // 레시피 C: 프라이팬 필요 (보유중)
        Recipe recipeC = Recipe.builder().category(category).recipeName("두부부침").build();
        recipeC.addRecipeIngredient(RecipeIngredient.builder().ingredient(tofu).quantity(java.math.BigDecimal.ONE).unit("모").build());
        recipeC.addRecipeTool(RecipeTool.builder().tool(pan).build());
        setRecipeId(recipeC, 30L);

        // 레시피 D: 오븐 필요 (보유 안 함) - matchCount/가중치는 C와 동일하게 맞춤
        Recipe recipeD = Recipe.builder().category(category).recipeName("두부오븐구이").build();
        recipeD.addRecipeIngredient(RecipeIngredient.builder().ingredient(tofu).quantity(java.math.BigDecimal.ONE).unit("모").build());
        recipeD.addRecipeTool(RecipeTool.builder().tool(oven).build());
        setRecipeId(recipeD, 40L);

        UserIngredient myTofu = UserIngredient.builder()
                .userId(1L).ingredient(tofu).quantity(java.math.BigDecimal.ONE).unit("모")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박 (가중치 0으로 동점 맞춤)
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myTofu));
        when(recipeRepository.findRecipesByMatchingIngredients(List.of(4L)))
                .thenReturn(List.of(matchResult(30L, 1L), matchResult(40L, 1L)));
        // 일부러 D를 먼저 반환해서, 정렬이 진짜로 도구 여부에 의해 뒤집히는지 검증
        when(recipeRepository.findAllById(List.of(30L, 40L)))
                .thenReturn(List.of(recipeD, recipeC));
        when(userToolRepository.findByUserId(1L))
                .thenReturn(List.of(UserTool.builder().userId(1L).tool(pan).build()));

        List<RecipeRecommendResponse> result = recipeService.recommendRecipes(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipeId()).isEqualTo(30L); // 보유 도구로 만들 수 있는 레시피가 1순위
        assertThat(result.get(1).getRecipeId()).isEqualTo(40L);
        }

        private void setToolId(CookingTool tool, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(tool, "toolId", id);
        }
}