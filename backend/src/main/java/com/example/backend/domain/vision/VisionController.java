package com.example.backend.domain.vision;

import com.example.backend.domain.vision.dto.RecognizedIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;

    // 카메라로 찍은 냉장고 재료 사진을 업로드하면, Vision API로 인식한 뒤 카테고리로 분류해 반환한다.
    // 아직 회원/재료 엔티티가 없어 DB 저장은 하지 않고 인식 결과만 내려준다.
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public ResponseEntity<List<RecognizedIngredientResponse>> recognize(
            @RequestParam("image") MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(visionService.recognizeIngredients(image));
    }
}
