package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/ingredients")
@RequiredArgsConstructor
public class UserIngredientController {

    private final UserIngredientService userIngredientService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 지금은 로그인이 아직 없어서, 테스트하기 편하게 쿼리파라미터로 userId를 임시로 받음.
    // 예: GET /api/v1/users/me/ingredients?userId=1
    @GetMapping
    public List<UserIngredientResponse> getMyIngredients(@RequestParam Long userId) {
        return userIngredientService.getMyIngredients(userId);
    }
}
