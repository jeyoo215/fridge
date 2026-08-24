package com.example.backend.domain.fridge;

import com.example.backend.domain.fridge.dto.FridgeItemCreateRequest;
import com.example.backend.domain.fridge.dto.FridgeItemPlaceRequest;
import com.example.backend.domain.fridge.dto.FridgeItemResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridge")
@RequiredArgsConstructor
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    // TODO: 로그인 붙으면 userId는 토큰에서. 지금은 쿼리파라미터로 임시.
    // 냉장고 조회
    @GetMapping
    public List<FridgeItemResponse> getMyFridge(@RequestParam("userId") Long userId) {
        return fridgeItemService.getMyFridge(userId);
    }

    // (가) 새 재료 등록 + 배치
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestParam("userId") Long userId,
                       @RequestBody FridgeItemCreateRequest request) {
        return fridgeItemService.createWithNewIngredient(userId, request);
    }

    // (나) 기존 재료 배치
    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    public Long place(@RequestParam("userId") Long userId,
                      @RequestBody FridgeItemPlaceRequest request) {
        return fridgeItemService.place(userId, request);
    }

    // 위치/구역 이동
    @PatchMapping("/{fridgeItemId}/move")
    public void move(@RequestParam("userId") Long userId,
                     @PathVariable Long fridgeItemId,
                     @RequestParam Double posX,
                     @RequestParam Double posY,
                     @RequestParam FridgeItem.Zone zone) {
        fridgeItemService.move(userId, fridgeItemId, posX, posY, zone);
    }

    // 냉장고에서 제거 (배치만, 보유재료는 유지)
    @DeleteMapping("/{fridgeItemId}")
    public void remove(@RequestParam("userId") Long userId,
                       @PathVariable Long fridgeItemId) {
        fridgeItemService.remove(userId, fridgeItemId);
    }
}