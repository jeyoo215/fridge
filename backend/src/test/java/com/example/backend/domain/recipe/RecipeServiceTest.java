package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.dto.RecipeRecommendPageResponse;
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

import java.math.BigDecimal;
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
    @DisplayName("보유 재료가 없으면 추천 목록은 빈 페이지다")
    void recommendRecipes_보유재료없으면_빈리스트() {
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of());

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("보유 재료는 있지만 겹치는 비조미료 재료가 하나도 없으면 빈 페이지다")
    void recommendRecipes_매칭레시피없으면_빈리스트() {
        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        setIngredientId(egg, 1L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(10))
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(1L)))
                .thenReturn(List.of());

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("필요한 비조미료 재료를 일부만 갖고 있으면 추천 목록에서 제외된다 (필수조건)")
    void recommendRecipes_일부만보유하면_제외() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        Ingredient onion = Ingredient.builder().ingredientName("양파").isSeasoning(false).build();
        setIngredientId(egg, 1L);
        setIngredientId(onion, 2L);

        // 레시피: 계란 + 양파 둘 다 필요한데, 유저는 계란만 보유
        Recipe recipe = Recipe.builder().category(category).recipeName("계란양파볶음").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipe, 50L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(10))
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));
        // 계란만 겹치니 matchCount=1
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(1L)))
                .thenReturn(List.of(matchResult(50L, 1L)));
        // 이 레시피는 비조미료 재료가 2개(계란+양파) 필요함
        when(recipeRepository.findNonSeasoningIngredientCountByRecipeIdIn(List.of(50L)))
                .thenReturn(List.of(matchResult(50L, 2L)));

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).isEmpty(); // 1개만 갖고 있어서 후보에서 빠져야 함
    }

    @Test
    @DisplayName("조미료는 필수조건에서 제외된다 (조미료가 없어도 나머지 재료만 다 있으면 추천됨)")
    void recommendRecipes_조미료는_필수조건에서제외() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        Ingredient salt = Ingredient.builder().ingredientName("소금").isSeasoning(true).build();
        setIngredientId(egg, 1L);
        setIngredientId(salt, 2L);

        // 레시피: 계란(비조미료) + 소금(조미료) 필요, 유저는 계란만 보유 (소금 없음)
        Recipe recipe = Recipe.builder().category(category).recipeName("계란찜").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(salt).quantity(BigDecimal.ONE).unit("꼬집").build());
        setRecipeId(recipe, 60L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(10))
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(1L)))
                .thenReturn(List.of(matchResult(60L, 1L))); // 비조미료 중 계란 1개 매칭
        when(recipeRepository.findNonSeasoningIngredientCountByRecipeIdIn(List.of(60L)))
                .thenReturn(List.of(matchResult(60L, 1L)));
        when(recipeRepository.findAllWithIngredientsByRecipeIdIn(List.of(60L)))
                .thenReturn(List.of(recipe));
        when(recipeRepository.findToolIdPairsByRecipeIdIn(List.of(60L)))
                .thenReturn(List.of());
        when(userToolRepository.findByUserId(1L))
                .thenReturn(List.of());

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).hasSize(1); // 소금 없어도 추천됨
        assertThat(result.getContent().get(0).getRecipeId()).isEqualTo(60L);
    }

    @Test
    @DisplayName("유통기한이 D-1인 재료를 쓰는 레시피가, 완전매칭이어도 유통기한 가중치로 더 높은 순위에 온다 (FR-21)")
    void recommendRecipes_유통기한임박재료_우선순위() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        Ingredient onion = Ingredient.builder().ingredientName("양파").isSeasoning(false).build();
        Ingredient garlic = Ingredient.builder().ingredientName("마늘").isSeasoning(false).build();
        setIngredientId(egg, 1L);
        setIngredientId(onion, 2L);
        setIngredientId(garlic, 3L);

        // 레시피 A: 계란만 씀 (D-1 임박 재료 활용, 가중치 3점), 완전매칭
        Recipe recipeA = Recipe.builder().category(category).recipeName("계란요리").build();
        recipeA.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipeA, 10L);

        // 레시피 B: 양파+마늘 씀 (임박 재료 하나도 안 씀, 가중치 0점), 완전매칭
        Recipe recipeB = Recipe.builder().category(category).recipeName("양파마늘볶음").build();
        recipeB.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        recipeB.addRecipeIngredient(RecipeIngredient.builder().ingredient(garlic).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipeB, 20L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(1)) // D-1
                .build();
        UserIngredient myOnion = UserIngredient.builder()
                .userId(1L).ingredient(onion).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박
                .build();
        UserIngredient myGarlic = UserIngredient.builder()
                .userId(1L).ingredient(garlic).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg, myOnion, myGarlic));
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(matchResult(10L, 1L), matchResult(20L, 2L)));
        when(recipeRepository.findNonSeasoningIngredientCountByRecipeIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(matchResult(10L, 1L), matchResult(20L, 2L)));
        when(recipeRepository.findAllWithIngredientsByRecipeIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(recipeA, recipeB));
        when(recipeRepository.findToolIdPairsByRecipeIdIn(List.of(10L, 20L)))
                .thenReturn(List.of());
        when(userToolRepository.findByUserId(1L))
                .thenReturn(List.of());

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getRecipeId()).isEqualTo(10L); // 유통기한 임박 재료 활용도가 더 높음
        assertThat(result.getContent().get(0).getExpiryPriorityScore()).isEqualTo(3);
        assertThat(result.getContent().get(1).getRecipeId()).isEqualTo(20L);
        assertThat(result.getContent().get(1).getExpiryPriorityScore()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("유통기한 점수가 같으면, 보유한 조리도구로 만들 수 있는 레시피가 우선순위가 높다 (FR-22)")
    void recommendRecipes_보유조리도구_우선순위() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();

        Ingredient tofu = Ingredient.builder().ingredientName("두부").isSeasoning(false).build();
        setIngredientId(tofu, 4L);

        CookingTool pan = CookingTool.builder().toolName("프라이팬").build();
        setToolId(pan, 1L);
        CookingTool oven = CookingTool.builder().toolName("오븐").build();
        setToolId(oven, 2L);

        // 레시피 C: 프라이팬 필요 (보유중), 완전매칭
        Recipe recipeC = Recipe.builder().category(category).recipeName("두부부침").build();
        recipeC.addRecipeIngredient(RecipeIngredient.builder().ingredient(tofu).quantity(BigDecimal.ONE).unit("모").build());
        setRecipeId(recipeC, 30L);

        // 레시피 D: 오븐 필요 (보유 안 함), 완전매칭
        Recipe recipeD = Recipe.builder().category(category).recipeName("두부오븐구이").build();
        recipeD.addRecipeIngredient(RecipeIngredient.builder().ingredient(tofu).quantity(BigDecimal.ONE).unit("모").build());
        setRecipeId(recipeD, 40L);

        UserIngredient myTofu = UserIngredient.builder()
                .userId(1L).ingredient(tofu).quantity(BigDecimal.ONE).unit("모")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20)) // 안 임박 (가중치 0으로 동점 맞춤)
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myTofu));
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(4L)))
                .thenReturn(List.of(matchResult(30L, 1L), matchResult(40L, 1L)));
        when(recipeRepository.findNonSeasoningIngredientCountByRecipeIdIn(List.of(30L, 40L)))
                .thenReturn(List.of(matchResult(30L, 1L), matchResult(40L, 1L)));
        // 일부러 D를 먼저 반환해서, 정렬이 진짜로 도구 여부에 의해 뒤집히는지 검증
        when(recipeRepository.findAllWithIngredientsByRecipeIdIn(List.of(30L, 40L)))
                .thenReturn(List.of(recipeD, recipeC));
        when(recipeRepository.findToolIdPairsByRecipeIdIn(List.of(30L, 40L)))
                .thenReturn(List.of(toolIdPair(30L, 1L), toolIdPair(40L, 2L)));
        when(userToolRepository.findByUserId(1L))
                .thenReturn(List.of(UserTool.builder().userId(1L).tool(pan).build()));

        RecipeRecommendPageResponse result = recipeService.recommendRecipes(1L, 0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getRecipeId()).isEqualTo(30L); // 보유 도구로 만들 수 있는 레시피가 1순위
        assertThat(result.getContent().get(1).getRecipeId()).isEqualTo(40L);
    }

    @Test
    @DisplayName("매칭 결과가 size보다 많으면 요청한 페이지 크기만큼만 잘라서 반환한다")
    void recommendRecipes_페이지네이션_정상동작() {
        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();
        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        setIngredientId(egg, 1L);

        // 레시피 3개, 전부 계란만 사용 (완전매칭) -> matchResult 순서 그대로 유지됨
        Recipe recipe1 = Recipe.builder().category(category).recipeName("레시피1").build();
        recipe1.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipe1, 1L);
        Recipe recipe2 = Recipe.builder().category(category).recipeName("레시피2").build();
        recipe2.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipe2, 2L);
        Recipe recipe3 = Recipe.builder().category(category).recipeName("레시피3").build();
        recipe3.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        setRecipeId(recipe3, 3L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(20))
                .build();

        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));
        when(recipeRepository.findRecipesByMatchingNonSeasoningIngredients(List.of(1L)))
                .thenReturn(List.of(matchResult(1L, 1L), matchResult(2L, 1L), matchResult(3L, 1L)));
        when(recipeRepository.findNonSeasoningIngredientCountByRecipeIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(matchResult(1L, 1L), matchResult(2L, 1L), matchResult(3L, 1L)));
        when(recipeRepository.findAllWithIngredientsByRecipeIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(recipe1, recipe2, recipe3));
        when(recipeRepository.findToolIdPairsByRecipeIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of());
        when(userToolRepository.findByUserId(1L))
                .thenReturn(List.of());

        RecipeRecommendPageResponse firstPage = recipeService.recommendRecipes(1L, 0, 2);

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
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

    private RecipeRepository.RecipeToolIdPair toolIdPair(Long recipeId, Long toolId) {
        return new RecipeRepository.RecipeToolIdPair() {
            public Long getRecipeId() { return recipeId; }
            public Long getToolId() { return toolId; }
        };
    }

    // 테스트에서만 필요한 id 세팅 (엔티티 id는 @GeneratedValue라 리플렉션으로 강제 주입)
    private void setIngredientId(Ingredient ingredient, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(ingredient, "ingredientId", id);
    }

    private void setRecipeId(Recipe recipe, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(recipe, "recipeId", id);
    }

    private void setToolId(CookingTool tool, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(tool, "toolId", id);
    }
}