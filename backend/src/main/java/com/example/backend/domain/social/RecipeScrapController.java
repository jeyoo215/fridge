package com.example.backend.domain.social;

import com.example.backend.domain.social.dto.ScrapedRecipeResponse;
import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecipeScrapController {

    private final RecipeScrapService recipeScrapService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: POST /api/v1/recipes/1/scraps?userId=1  (누를 때마다 스크랩<->취소 토글)
    @PostMapping("/api/v1/recipes/{recipeId}/scraps")
    public ToggleResponse toggle(@PathVariable("recipeId") Long recipeId,
                                  @RequestParam("userId") Long userId) {
        return recipeScrapService.toggle(userId, recipeId);
    }

    // 예: GET /api/v1/recipes/1/scraps?userId=1
    @GetMapping("/api/v1/recipes/{recipeId}/scraps")
    public ToggleResponse getStatus(@PathVariable("recipeId") Long recipeId,
                                     @RequestParam("userId") Long userId) {
        return recipeScrapService.getStatus(userId, recipeId);
    }

    // 마이페이지 "내가 스크랩한 레시피" 목록
    // 예: GET /api/v1/users/me/scraps?userId=1
    @GetMapping("/api/v1/users/me/scraps")
    public List<ScrapedRecipeResponse> getMyScraps(@RequestParam("userId") Long userId) {
        return recipeScrapService.getMyScraps(userId);
    }
}
