package com.example.backend.domain.review;

import com.example.backend.domain.review.dto.MyRecipeReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 마이페이지 "내가 평가한 레시피" 조회 전용 컨트롤러.
// 기존 RecipeReviewController가 클래스 레벨에 /api/v1/recipes/{recipeId}/reviews 경로를 갖고 있어서,
// 그 안에 절대경로를 추가하면 경로가 이상하게 합쳐지는 문제가 있어 별도 컨트롤러로 분리함.
@RestController
@RequiredArgsConstructor
public class UserRecipeReviewController {

    private final RecipeReviewService recipeReviewService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 예: GET /api/v1/users/me/recipe-reviews?userId=1
    @GetMapping("/api/v1/users/me/recipe-reviews")
    public List<MyRecipeReviewResponse> getMyReviews(@RequestParam("userId") Long userId) {
        return recipeReviewService.getMyReviews(userId);
    }
}
