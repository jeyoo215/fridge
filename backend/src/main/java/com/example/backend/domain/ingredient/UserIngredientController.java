package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/ingredients")
@RequiredArgsConstructor
public class UserIngredientController {

    private final UserIngredientService userIngredientService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    // 지금은 로그인이 아직 없어서, 테스트하기 편하게 쿼리파라미터로 userId를 임시로 받음.
    @GetMapping
    public List<UserIngredientResponse> getMyIngredients(@RequestParam Long userId) {
        return userIngredientService.getMyIngredients(userId);
    }

    // 재료 수동 등록
    // 예: POST /api/v1/users/me/ingredients?userId=1
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@RequestParam Long userId,
                                       @Valid @RequestBody UserIngredientRegisterRequest request) {
        Long userIngredientId = userIngredientService.register(userId, request);
        return Map.of("userIngredientId", userIngredientId);
    }
}
