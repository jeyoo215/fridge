package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeCategory;
import com.example.backend.domain.recipe.RecipeIngredient;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.shopping.dto.ShoppingListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private UserIngredientRepository userIngredientRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    @DisplayName("레시피 필요 재료 중 보유하지 않은 것만 부족 재료로 담긴다 (FR-30)")
    void getShoppingList_보유하지않은재료만_부족재료() {
        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        Ingredient onion = Ingredient.builder().ingredientName("양파").isSeasoning(false).build();
        ReflectionTestUtils.setField(egg, "ingredientId", 1L);
        ReflectionTestUtils.setField(onion, "ingredientId", 2L);

        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();
        Recipe recipe = Recipe.builder().category(category).recipeName("계란양파볶음").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.valueOf(2)).unit("개").build());
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        ReflectionTestUtils.setField(recipe, "recipeId", 1L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(5))
                .build(); // 계란은 이미 보유중, 양파는 없음

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));

        ShoppingListResponse response = shoppingListService.getShoppingList(1L, 1L);

        assertThat(response.getMissingIngredients()).hasSize(1);
        assertThat(response.getMissingIngredients().get(0).getIngredientName()).isEqualTo("양파");
    }

    @Test
    @DisplayName("필요 재료를 다 갖고 있으면 부족 재료 목록은 비어있다")
    void getShoppingList_다갖고있으면_빈목록() {
        Ingredient egg = Ingredient.builder().ingredientName("계란").isSeasoning(false).build();
        ReflectionTestUtils.setField(egg, "ingredientId", 1L);

        RecipeCategory category = RecipeCategory.builder().categoryName("한식").build();
        Recipe recipe = Recipe.builder().category(category).recipeName("계란찜").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        ReflectionTestUtils.setField(recipe, "recipeId", 1L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개")
                .purchaseDate(LocalDate.now()).expirationDate(LocalDate.now().plusDays(5))
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg));

        ShoppingListResponse response = shoppingListService.getShoppingList(1L, 1L);

        assertThat(response.getMissingIngredients()).isEmpty();
    }
}