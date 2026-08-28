package com.example.backend.domain.fridge;

import com.example.backend.domain.fridge.dto.FridgeItemCreateRequest;
import com.example.backend.domain.fridge.dto.FridgeItemPlaceRequest;
import com.example.backend.domain.fridge.dto.FridgeItemResponse;
import com.example.backend.domain.ingredient.UserIngredient;
import com.example.backend.domain.ingredient.UserIngredientRepository;
import com.example.backend.domain.ingredient.UserIngredientService;
import com.example.backend.domain.ingredient.dto.UserIngredientRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeItemService {

    private final FridgeItemRepository fridgeItemRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final UserIngredientService userIngredientService; // 재료 등록 재사용

    // 냉장고 조회 — 배치된 재료 전부
    public List<FridgeItemResponse> getMyFridge(Long userId) {
        return fridgeItemRepository.findAllByUserId(userId).stream()
                .map(FridgeItemResponse::new)
                .toList();
    }

    // (가) 새 재료 등록 + 배치 한 번에
    @Transactional
    public Long createWithNewIngredient(Long userId, FridgeItemCreateRequest req) {
        // 1. 재료 등록은 페어1 서비스 재사용
        UserIngredientRegisterRequest registerReq = new UserIngredientRegisterRequest(
                req.ingredientId(), req.quantity(), req.unit(),
                req.purchaseDate(), req.expirationDate());
        Long userIngredientId = userIngredientService.register(userId, registerReq);

        // 2. 방금 등록한 보유재료 조회해서 배치
        UserIngredient ui = userIngredientRepository.findById(userIngredientId)
                .orElseThrow(() -> new IllegalStateException("재료 등록 직후 조회 실패"));

        FridgeItem item = FridgeItem.builder()
                .userIngredient(ui)
                .imageUrl(req.imageUrl())
                .imageType(req.imageType())
                .posX(req.posX())
                .posY(req.posY())
                .zone(req.zone())
                .build();

        return fridgeItemRepository.save(item).getFridgeItemId();
    }

    // (나) 이미 있는 재료 배치
    @Transactional
    public Long place(Long userId, FridgeItemPlaceRequest req) {
        UserIngredient ui = userIngredientRepository.findById(req.userIngredientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보유재료입니다."));

        if (!ui.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 재료만 배치할 수 있습니다.");
        }
        if (fridgeItemRepository.existsByUserIngredient_UserIngredientId(req.userIngredientId())) {
            throw new IllegalArgumentException("이미 냉장고에 배치된 재료입니다.");
        }

        FridgeItem item = FridgeItem.builder()
                .userIngredient(ui)
                .imageUrl(req.imageUrl())
                .imageType(req.imageType())
                .posX(req.posX())
                .posY(req.posY())
                .zone(req.zone())
                .build();

        return fridgeItemRepository.save(item).getFridgeItemId();
    }

    // 위치/구역 이동
    @Transactional
    public void move(Long userId, Long fridgeItemId, Double posX, Double posY, FridgeItem.Zone zone) {
        FridgeItem item = findOwned(userId, fridgeItemId);
        item.moveTo(posX, posY, zone);
    }

    // 냉장고에서 제거 (배치만 제거, 보유재료 자체는 안 지움)
    @Transactional
    public void remove(Long userId, Long fridgeItemId) {
        FridgeItem item = findOwned(userId, fridgeItemId);
        fridgeItemRepository.delete(item);
    }

    private FridgeItem findOwned(Long userId, Long fridgeItemId) {
        FridgeItem item = fridgeItemRepository.findById(fridgeItemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배치입니다."));
        if (!item.getUserIngredient().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 냉장고만 수정할 수 있습니다.");
        }
        return item;
    }
}