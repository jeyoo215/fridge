package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.FridgeNameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/fridge")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    // TODO: 로그인 기능 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @GetMapping
    public Map<String, String> getFridgeName(@RequestParam("userId") Long userId) {
        return Map.of("fridgeName", fridgeService.getFridgeName(userId));
    }

    @PatchMapping
    public Map<String, String> updateFridgeName(@RequestParam("userId") Long userId,
                                                 @Valid @RequestBody FridgeNameRequest request) {
        return Map.of("fridgeName", fridgeService.updateFridgeName(userId, request));
    }
}
