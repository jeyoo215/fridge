package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.FridgeNameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/fridge")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping
    public Map<String, String> getFridgeName(@AuthenticationPrincipal Long userId) {
        return Map.of("fridgeName", fridgeService.getFridgeName(userId));
    }

    @PatchMapping
    public Map<String, String> updateFridgeName(@AuthenticationPrincipal Long userId,
                                                 @Valid @RequestBody FridgeNameRequest request) {
        return Map.of("fridgeName", fridgeService.updateFridgeName(userId, request));
    }
}
