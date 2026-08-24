package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.UserAllergyIngredientRegisterRequest;
import com.example.backend.domain.user.dto.UserAllergyIngredientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/allergy-ingredients")
@RequiredArgsConstructor
public class UserAllergyIngredientController {

    private final UserAllergyIngredientService userAllergyIngredientService;

    // 마이페이지: 내 알레르기/기피 재료 목록
    @GetMapping
    public List<UserAllergyIngredientResponse> getMyAllergyIngredients(@AuthenticationPrincipal Long userId) {
        return userAllergyIngredientService.getMyAllergyIngredients(userId);
    }

    // 알레르기/기피 재료 직접 입력 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody UserAllergyIngredientRegisterRequest request) {
        Long id = userAllergyIngredientService.register(userId, request);
        return Map.of("id", id);
    }

    // 등록한 항목 삭제
    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable("id") Long id) {
        userAllergyIngredientService.delete(userId, id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
