package com.example.backend.domain.shopping;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.recipe.Recipe;
import com.example.backend.domain.recipe.RecipeIngredient;
import com.example.backend.domain.recipe.RecipeRepository;
import com.example.backend.domain.shopping.dto.ManualShoppingItemRequest;
import com.example.backend.domain.shopping.dto.MyShoppingListResponse;
import com.example.backend.domain.shopping.dto.ShoppingListResponse;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private UserIngredientRepository userIngredientRepository;
    @Mock private ShoppingListRepository shoppingListRepository;
    @Mock private IngredientRepository ingredientRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    @DisplayName("존재하지 않는 레시피로 조회하면 예외가 발생한다")
    void getShoppingList_존재하지않는레시피면_예외() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.getShoppingList(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("보유하지 않은 재료만 부족한 재료로 계산된다 (FR-30)")
    void getShoppingList_부족한재료만_반환() {
        Ingredient egg = ingredient(1L, "계란");
        Ingredient onion = ingredient(2L, "양파");

        Recipe recipe = Recipe.builder().recipeName("계란볶음").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(egg).quantity(BigDecimal.ONE).unit("개").build());
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        ReflectionTestUtils.setField(recipe, "recipeId", 10L);

        UserIngredient myEgg = UserIngredient.builder()
                .userId(1L).ingredient(egg).quantity(BigDecimal.ONE).unit("개").build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of(myEgg)); // 계란만 보유중, 양파는 없음

        ShoppingListResponse result = shoppingListService.getShoppingList(1L, 10L);

        assertThat(result.getMissingIngredients()).hasSize(1);
        assertThat(result.getMissingIngredients().get(0).getIngredientId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("장보기 리스트가 없는 유저는 부족한 재료를 담을 때 리스트가 새로 생성된다")
    void addMissingIngredientsToMyList_리스트없으면_새로생성() {
        Ingredient onion = ingredient(2L, "양파");
        Recipe recipe = Recipe.builder().recipeName("양파볶음").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        ReflectionTestUtils.setField(recipe, "recipeId", 10L);

        ShoppingList newList = ShoppingList.builder().userId(1L).build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of());
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(shoppingListRepository.save(any(ShoppingList.class))).thenReturn(newList);

        shoppingListService.addMissingIngredientsToMyList(1L, 10L);

        assertThat(newList.getItems()).hasSize(1);
        assertThat(newList.getItems().get(0).getIngredient().getIngredientId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미 리스트에 담긴 재료는 중복으로 담기지 않는다")
    void addMissingIngredientsToMyList_이미담긴재료는_중복안됨() {
        Ingredient onion = ingredient(2L, "양파");
        Recipe recipe = Recipe.builder().recipeName("양파볶음").build();
        recipe.addRecipeIngredient(RecipeIngredient.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());
        ReflectionTestUtils.setField(recipe, "recipeId", 10L);

        ShoppingList existingList = ShoppingList.builder().userId(1L).build();
        existingList.addItem(ShoppingListItem.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(userIngredientRepository.findByUserIdAndStatusOrderByExpirationDateAsc(1L, UserIngredient.Status.보유중))
                .thenReturn(List.of());
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(existingList));

        shoppingListService.addMissingIngredientsToMyList(1L, 10L);

        assertThat(existingList.getItems()).hasSize(1); // 늘어나지 않음
    }

    @Test
    @DisplayName("리스트가 없는 유저는 빈 리스트를 응답받는다")
    void getMyShoppingList_리스트없으면_빈응답() {
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MyShoppingListResponse result = shoppingListService.getMyShoppingList(1L);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("본인 소유 항목을 체크하면 checked가 true가 된다")
    void checkItem_정상체크() {
        Ingredient onion = ingredient(2L, "양파");
        ShoppingList list = ShoppingList.builder().userId(1L).build();
        ShoppingListItem item = ShoppingListItem.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build();
        list.addItem(item);
        ReflectionTestUtils.setField(item, "itemId", 100L);

        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(list));

        shoppingListService.checkItem(1L, 100L);

        assertThat(item.isChecked()).isTrue();
    }

    @Test
    @DisplayName("본인 소유가 아니거나 존재하지 않는 항목을 체크하려 하면 예외가 발생한다")
    void checkItem_존재하지않는항목이면_예외() {
        ShoppingList list = ShoppingList.builder().userId(1L).build();
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(list));

        assertThatThrownBy(() -> shoppingListService.checkItem(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("장보기 리스트 자체가 없는 유저가 항목을 삭제하려 하면 예외가 발생한다")
    void deleteItem_리스트없으면_예외() {
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.deleteItem(1L, 100L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("항목을 삭제하면 리스트에서 제거된다")
    void deleteItem_정상삭제() {
        Ingredient onion = ingredient(2L, "양파");
        ShoppingList list = ShoppingList.builder().userId(1L).build();
        ShoppingListItem item = ShoppingListItem.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build();
        list.addItem(item);
        ReflectionTestUtils.setField(item, "itemId", 100L);

        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(list));

        shoppingListService.deleteItem(1L, 100L);

        assertThat(list.getItems()).isEmpty();
    }

    private Ingredient ingredient(Long id, String name) {
        Ingredient ingredient = Ingredient.builder().ingredientName(name).isSeasoning(false).build();
        ReflectionTestUtils.setField(ingredient, "ingredientId", id);
        return ingredient;
    }

    @Test
    @DisplayName("셀프로 재료를 검색해서 담으면 리스트에 새 항목으로 추가된다")
    void addManualItem_새재료_정상추가() {
        Ingredient onion = ingredient(2L, "양파");
        ShoppingList list = ShoppingList.builder().userId(1L).build();

        when(ingredientRepository.findById(2L)).thenReturn(Optional.of(onion));
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(list));

        shoppingListService.addManualItem(1L, new ManualShoppingItemRequest(2L, BigDecimal.ONE, "개"));

        assertThat(list.getItems()).hasSize(1);
        assertThat(list.getItems().get(0).getIngredient().getIngredientId()).isEqualTo(2L);
        assertThat(list.getItems().get(0).getQuantity()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("장보기 리스트가 없는 유저가 셀프로 담으면 리스트가 새로 생성된다")
    void addManualItem_리스트없으면_새로생성() {
        Ingredient onion = ingredient(2L, "양파");
        ShoppingList newList = ShoppingList.builder().userId(1L).build();

        when(ingredientRepository.findById(2L)).thenReturn(Optional.of(onion));
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(shoppingListRepository.save(any(ShoppingList.class))).thenReturn(newList);

        shoppingListService.addManualItem(1L, new ManualShoppingItemRequest(2L, BigDecimal.ONE, "개"));

        assertThat(newList.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("이미 담긴 재료를 셀프로 또 담으면 항목 수는 그대로이고 수량만 합산된다")
    void addManualItem_이미담긴재료는_수량합산() {
        Ingredient onion = ingredient(2L, "양파");
        ShoppingList list = ShoppingList.builder().userId(1L).build();
        list.addItem(ShoppingListItem.builder().ingredient(onion).quantity(BigDecimal.ONE).unit("개").build());

        when(ingredientRepository.findById(2L)).thenReturn(Optional.of(onion));
        when(shoppingListRepository.findByUserId(1L)).thenReturn(Optional.of(list));

        shoppingListService.addManualItem(1L, new ManualShoppingItemRequest(2L, BigDecimal.ONE, "개"));

        assertThat(list.getItems()).hasSize(1); // 새 항목으로 안 늘어남
        assertThat(list.getItems().get(0).getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    @DisplayName("존재하지 않는 재료를 셀프로 담으려 하면 예외가 발생한다")
    void addManualItem_존재하지않는재료면_예외() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                shoppingListService.addManualItem(1L, new ManualShoppingItemRequest(999L, BigDecimal.ONE, "개"))
        ).isInstanceOf(IllegalArgumentException.class);
    }
}