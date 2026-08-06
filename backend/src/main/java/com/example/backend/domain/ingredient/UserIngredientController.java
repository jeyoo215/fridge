package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import com.example.backend.domain.ingredient.dto.UserIngredientUpdateRequest;
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
    public List<UserIngredientResponse> getMyIngredients(@RequestParam("userId") Long userId) {
        return userIngredientService.getMyIngredients(userId);
    }

    // 재료 수동 등록
    // 예: POST /api/v1/users/me/ingredients?userId=1
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@RequestParam("userId") Long userId,
                                       @Valid @RequestBody UserIngredientRegisterRequest request) {
        Long userIngredientId = userIngredientService.register(userId, request);
        return Map.of("userIngredientId", userIngredientId);
    }

    // 재료 수정 (수량/유통기한)
    // 예: PATCH /api/v1/users/me/ingredients/1?userId=1
    @PatchMapping("/{userIngredientId}")
    public void update(@RequestParam("userId") Long userId,
                        @PathVariable("userIngredientId") Long userIngredientId,
                        @Valid @RequestBody UserIngredientUpdateRequest request) {
        userIngredientService.update(userId, userIngredientId, request);
    }

    // 소진 처리
    // 예: PATCH /api/v1/users/me/ingredients/1/consume?userId=1
    @PatchMapping("/{userIngredientId}/consume")
    public void consume(@RequestParam("userId") Long userId,
                         @PathVariable("userIngredientId") Long userIngredientId) {
        userIngredientService.consume(userId, userIngredientId);
    }

    // 폐기 처리
    // 예: PATCH /api/v1/users/me/ingredients/1/discard?userId=1
    @PatchMapping("/{userIngredientId}/discard")
    public void discard(@RequestParam("userId") Long userId,
                         @PathVariable("userIngredientId") Long userIngredientId) {
        userIngredientService.discard(userId, userIngredientId);
    }

    // 완전 삭제
    // 예: DELETE /api/v1/users/me/ingredients/1?userId=1
    @DeleteMapping("/{userIngredientId}")
    public void delete(@RequestParam("userId") Long userId,
                        @PathVariable("userIngredientId") Long userIngredientId) {
        userIngredientService.delete(userId, userIngredientId);
    }
}
