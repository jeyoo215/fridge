package com.example.backend.domain.vision;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;

    // 카메라로 찍은 냉장고 재료 사진을 업로드하면, Vision API로 인식한 뒤 카테고리로 분류해 반환한다.
    // 아직 회원/재료 엔티티가 없어 DB 저장은 하지 않고 인식 결과만 내려준다.
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public ResponseEntity<Object> recognize(@RequestParam("image") MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "업로드된 이미지가 없습니다."));
        }
        return ResponseEntity.ok(visionService.recognizeIngredients(image));
    }

    // Vision API 자체가 실패한 경우(인증 오류, 쿼터 초과, 이미지 형식 문제 등).
    // 우리 요청이 잘못된 게 아니라 외부 서비스 쪽 문제라 400이 아닌 502로 구분한다.
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleVisionFailure(IllegalStateException e) {
        return Map.of("message", e.getMessage());
    }
}
