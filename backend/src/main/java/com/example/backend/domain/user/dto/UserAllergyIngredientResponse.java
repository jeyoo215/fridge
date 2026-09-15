package com.example.backend.domain.user.dto;

import com.example.backend.domain.user.UserAllergyIngredient;
import lombok.Getter;

@Getter
public class UserAllergyIngredientResponse {

    private final Long id;
    private final String ingredientName;
    private final UserAllergyIngredient.Type type;

    public UserAllergyIngredientResponse(UserAllergyIngredient entity) {
        this.id = entity.getId();
        this.ingredientName = entity.getIngredientName();
        this.type = entity.getType();
    }
}
