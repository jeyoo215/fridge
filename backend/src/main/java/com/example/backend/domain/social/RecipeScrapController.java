package com.example.backend.domain.social;

import com.example.backend.domain.social.dto.ScrapedRecipeResponse;
import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecipeScrapController {

    private final RecipeScrapService recipeScrapService;

    // 예: POST /api/v1/recipes/1/scraps  (누를 때마다 스크랩<->취소 토글)
    @PostMapping("/api/v1/recipes/{recipeId}/scraps")
    public ToggleResponse toggle(@PathVariable("recipeId") Long recipeId,
                                  @AuthenticationPrincipal Long userId) {
        return recipeScrapService.toggle(userId, recipeId);
    }

    // 예: GET /api/v1/recipes/1/scraps
    @GetMapping("/api/v1/recipes/{recipeId}/scraps")
    public ToggleResponse getStatus(@PathVariable("recipeId") Long recipeId,
                                     @AuthenticationPrincipal Long userId) {
        return recipeScrapService.getStatus(userId, recipeId);
    }

    // 마이페이지 "내가 스크랩한 레시피" 목록
    // 예: GET /api/v1/users/me/scraps
    @GetMapping("/api/v1/users/me/scraps")
    public List<ScrapedRecipeResponse> getMyScraps(@AuthenticationPrincipal Long userId) {
        return recipeScrapService.getMyScraps(userId);
    }
}
