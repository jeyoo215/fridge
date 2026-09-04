package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.RecognizedIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/ingredients")
@RequiredArgsConstructor
public class ImageRecognitionController {

    private final ImageRecognitionService imageRecognitionService;

    // 카메라로 찍은 재료 사진을 보내면, 후보 재료 목록(신뢰도 포함)을 반환.
    // 여기서 바로 등록되는 게 아니라, 사용자가 화면에서 후보를 골라야 최종 등록됨 (오인식 방지).
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public List<RecognizedIngredientResponse> recognize(@AuthenticationPrincipal Long userId,
                                                          @RequestParam("image") MultipartFile image) {
        return imageRecognitionService.recognize(userId, image);
    }

    // Vision API 자체가 실패한 경우(인증 오류, 쿼터 초과, 이미지 형식 문제 등).
    // 우리 요청이 잘못된 게 아니라 외부 서비스 쪽 문제라 400이 아닌 502로 구분한다.
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleVisionFailure(IllegalStateException e) {
        return Map.of("message", e.getMessage());
    }
}
