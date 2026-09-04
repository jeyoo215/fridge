package com.example.backend.domain.social;

import com.example.backend.domain.social.dto.MadeRecipeResponse;
import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecipeCookRecordController {

    private final RecipeCookRecordService recipeCookRecordService;

    // 예: POST /api/v1/recipes/1/cook-records  (누를 때마다 만듦<->취소 토글)
    @PostMapping("/api/v1/recipes/{recipeId}/cook-records")
    public ToggleResponse toggle(@PathVariable("recipeId") Long recipeId,
                                  @AuthenticationPrincipal Long userId) {
        return recipeCookRecordService.toggle(userId, recipeId);
    }

    // 예: GET /api/v1/recipes/1/cook-records
    @GetMapping("/api/v1/recipes/{recipeId}/cook-records")
    public ToggleResponse getStatus(@PathVariable("recipeId") Long recipeId,
                                     @AuthenticationPrincipal Long userId) {
        return recipeCookRecordService.getStatus(userId, recipeId);
    }

    // 마이페이지 "내가 만들어본 레시피" 목록
    // 예: GET /api/v1/users/me/made-recipes
    @GetMapping("/api/v1/users/me/made-recipes")
    public List<MadeRecipeResponse> getMyMadeRecipes(@AuthenticationPrincipal Long userId) {
        return recipeCookRecordService.getMyMadeRecipes(userId);
    }
}
