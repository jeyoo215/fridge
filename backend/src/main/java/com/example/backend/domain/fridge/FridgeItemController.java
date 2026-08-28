package com.example.backend.domain.fridge;

import com.example.backend.domain.fridge.dto.FridgeItemCreateRequest;
import com.example.backend.domain.fridge.dto.FridgeItemPlaceRequest;
import com.example.backend.domain.fridge.dto.FridgeItemResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridge")
@RequiredArgsConstructor
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    @GetMapping("/items")
    public List<FridgeItemResponse> getMyFridge(@AuthenticationPrincipal Long userId) {
        return fridgeItemService.getMyFridge(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@AuthenticationPrincipal Long userId,
                       @RequestBody FridgeItemCreateRequest request) {
        return fridgeItemService.createWithNewIngredient(userId, request);
    }

    @PostMapping("/items/place")
    @ResponseStatus(HttpStatus.CREATED)
    public Long place(@AuthenticationPrincipal Long userId,
                      @RequestBody FridgeItemPlaceRequest request) {
        return fridgeItemService.place(userId, request);
    }

    @PatchMapping("/items/{fridgeItemId}/move")
    public void move(@AuthenticationPrincipal Long userId,
                     @PathVariable Long fridgeItemId,
                     @RequestParam Double posX,
                     @RequestParam Double posY,
                     @RequestParam FridgeItem.Zone zone) {
        fridgeItemService.move(userId, fridgeItemId, posX, posY, zone);
    }

    @DeleteMapping("/items/{fridgeItemId}")
    public void remove(@AuthenticationPrincipal Long userId,
                       @PathVariable Long fridgeItemId) {
        fridgeItemService.remove(userId, fridgeItemId);
    }
}