package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.UserAllergyIngredientRegisterRequest;
import com.example.backend.domain.user.dto.UserAllergyIngredientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/allergy-ingredients")
@RequiredArgsConstructor
public class UserAllergyIngredientController {

    private final UserAllergyIngredientService userAllergyIngredientService;

    // 마이페이지: 내 알레르기/기피 재료 목록
    // 예: GET /api/v1/users/me/allergy-ingredients?userId=1
    @GetMapping
    public List<UserAllergyIngredientResponse> getMyAllergyIngredients(@RequestParam("userId") Long userId) {
        return userAllergyIngredientService.getMyAllergyIngredients(userId);
    }

    // 알레르기/기피 재료 직접 입력 등록
    // 예: POST /api/v1/users/me/allergy-ingredients?userId=1
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@RequestParam("userId") Long userId,
                                       @Valid @RequestBody UserAllergyIngredientRegisterRequest request) {
        Long id = userAllergyIngredientService.register(userId, request);
        return Map.of("id", id);
    }

    // 등록한 항목 삭제
    // 예: DELETE /api/v1/users/me/allergy-ingredients/1?userId=1
    @DeleteMapping("/{id}")
    public void delete(@RequestParam("userId") Long userId, @PathVariable("id") Long id) {
        userAllergyIngredientService.delete(userId, id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
