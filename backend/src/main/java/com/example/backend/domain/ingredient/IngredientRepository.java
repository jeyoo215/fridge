package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findTop10ByIngredientNameContaining(String keyword);

    Optional<Ingredient> findByIngredientName(String ingredientName);
}