package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;
import com.example.backend.domain.ingredient.dto.UserIngredientResponse;
import com.example.backend.domain.ingredient.dto.UserIngredientUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/ingredients")
@RequiredArgsConstructor
public class UserIngredientController {

    private final UserIngredientService userIngredientService;

    // JwtAuthenticationFilter가 SecurityContext에 넣어준 userId(Long)를 그대로 principal로 받음
    @GetMapping
    public List<UserIngredientResponse> getMyIngredients(@AuthenticationPrincipal Long userId) {
        return userIngredientService.getMyIngredients(userId);
    }

    // 재료 수동 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody UserIngredientRegisterRequest request) {
        Long userIngredientId = userIngredientService.register(userId, request);
        return Map.of("userIngredientId", userIngredientId);
    }

    // 재료 수정 (수량/유통기한)
    @PatchMapping("/{userIngredientId}")
    public void update(@AuthenticationPrincipal Long userId,
                        @PathVariable("userIngredientId") Long userIngredientId,
                        @Valid @RequestBody UserIngredientUpdateRequest request) {
        userIngredientService.update(userId, userIngredientId, request);
    }

    // 재료 삭제 (사용완료/폐기 구분 없이 "삭제" 하나로 통합)
    @DeleteMapping("/{userIngredientId}")
    public void delete(@AuthenticationPrincipal Long userId,
                        @PathVariable("userIngredientId") Long userIngredientId) {
        userIngredientService.delete(userId, userIngredientId);
    }

    // 존재하지 않는 재료를 건드리거나, 본인 소유가 아닌 재료를 건드리려 할 때 400으로 명확히 응답
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
