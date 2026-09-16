package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.FridgeNameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeService {

    private static final String DEFAULT_FRIDGE_NAME = "내 냉장고";

    private final FridgeRepository fridgeRepository;

    // 냉장고 이름 조회. 아직 한 번도 만든 적 없으면(첫 방문) 기본 이름으로 자동 생성해서 반환
    @Transactional
    public String getFridgeName(Long userId) {
        return fridgeRepository.findByUserId(userId)
                .orElseGet(() -> fridgeRepository.save(
                        Fridge.builder().userId(userId).fridgeName(DEFAULT_FRIDGE_NAME).build()
                ))
                .getFridgeName();
    }

    // 냉장고 이름 변경
    @Transactional
    public String updateFridgeName(Long userId, FridgeNameRequest request) {
        Fridge fridge = fridgeRepository.findByUserId(userId)
                .orElseGet(() -> fridgeRepository.save(
                        Fridge.builder().userId(userId).fridgeName(DEFAULT_FRIDGE_NAME).build()
                ));
        fridge.rename(request.fridgeName());
        return fridge.getFridgeName();
    }
}
