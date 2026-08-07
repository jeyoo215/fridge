package com.example.backend.domain.social;

import com.example.backend.domain.social.dto.ToggleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/likes")
@RequiredArgsConstructor
public class RecipeLikeController {

    private final RecipeLikeService recipeLikeService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: POST /api/v1/recipes/1/likes?userId=1  (누를 때마다 좋아요<->취소 토글)
    @PostMapping
    public ToggleResponse toggle(@PathVariable("recipeId") Long recipeId,
                                  @RequestParam("userId") Long userId) {
        return recipeLikeService.toggle(userId, recipeId);
    }

    // 예: GET /api/v1/recipes/1/likes?userId=1  (지금 좋아요 상태 + 총 개수)
    @GetMapping
    public ToggleResponse getStatus(@PathVariable("recipeId") Long recipeId,
                                     @RequestParam("userId") Long userId) {
        return recipeLikeService.getStatus(userId, recipeId);
    }
}
