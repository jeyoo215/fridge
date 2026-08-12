package com.example.backend.domain.recipe;

import com.example.backend.domain.ingredient.Ingredient;
import com.example.backend.domain.ingredient.IngredientRepository;
import com.example.backend.domain.recipe.dto.api.ParsedIngredient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeParsingWorker {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public void saveParsedIngredients(Long recipeId, List<ParsedIngredient> parsed) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow();
        for (ParsedIngredient p : parsed) {
            Ingredient ingredient = resolveIngredient(p);
            recipe.addRecipeIngredient(RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(p.getQuantity() == null ? null : BigDecimal.valueOf(p.getQuantity()))
                    .unit(p.getUnit())
                    .build());
        }
        recipeRepository.save(recipe);
    }

    private Ingredient resolveIngredient(ParsedIngredient p) {
        return ingredientRepository.findByIngredientName(p.getName())
                .orElseGet(() -> ingredientRepository.save(
                        Ingredient.builder()
                                .ingredientName(p.getName())
                                .isSeasoning(p.isSeasoning())
                                .build()));
    }

    // 조리순서 저장 (external_id로 레시피 찾아서). 저장 true, 이미 있거나 없으면 false
    @Transactional
    public boolean saveSteps(String externalId, java.util.List<com.example.backend.domain.recipe.dto.api.CookRcpRow.Step> steps) {
        Recipe recipe = recipeRepository.findBySourceAndExternalId("식약처", externalId).orElse(null);
        if (recipe == null) return false;
        if (!recipe.getCookingSteps().isEmpty()) return false;
        if (steps.isEmpty()) return false;

        int order = 1;
        for (var step : steps) {
            recipe.addCookingStep(CookingStep.builder()
                    .stepOrder(order++)
                    .description(step.description())
                    .imageUrl(step.imageUrl())
                    .build());
        }
        recipeRepository.save(recipe);
        return true;
    }
}