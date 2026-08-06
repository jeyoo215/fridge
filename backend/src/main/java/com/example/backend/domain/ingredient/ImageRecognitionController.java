package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.RecognizedIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/ingredients")
@RequiredArgsConstructor
public class ImageRecognitionController {

    private final ImageRecognitionService imageRecognitionService;

    // 카메라로 찍은 재료 사진을 보내면, 후보 재료 목록(신뢰도 포함)을 반환.
    // 여기서 바로 등록되는 게 아니라, 사용자가 화면에서 후보를 골라야 최종 등록됨 (오인식 방지).
    // 예: POST /api/v1/users/me/ingredients/recognize?userId=1  (multipart/form-data, key: image)
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public List<RecognizedIngredientResponse> recognize(@RequestParam("userId") Long userId,
                                                          @RequestParam("image") MultipartFile image) {
        return imageRecognitionService.recognize(userId, image);
    }
}
