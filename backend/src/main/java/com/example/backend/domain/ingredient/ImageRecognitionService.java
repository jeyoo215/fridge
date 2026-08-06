package com.example.backend.domain.ingredient;

import com.example.backend.domain.ingredient.dto.RecognizedIngredientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageRecognitionService {

    // TODO(Vision API 연동): 지금은 실제 이미지 분석 대신 재료 마스터에서 무작위로 후보를 뽑는 임시(스텁) 로직임.
    // 실제 연동 시 이 메서드 내부만 아래처럼 교체하면 됨:
    //   1) Google Cloud Vision / AWS Rekognition 클라이언트로 이미지 라벨 분석 요청
    //   2) 반환된 영문 라벨(onion, tomato 등)을 ingredient 테이블의 영문 라벨 컬럼과 매칭
    //   3) 매칭된 재료 + API가 준 confidence 그대로 RecognizedIngredientResponse로 변환
    // 화면(프론트) 쪽은 이 메서드의 반환 타입만 유지되면 전혀 안 바뀌어도 됨.
    private static final String STUB_PROVIDER = "STUB(임시)"; // 실제 연동 시 "GoogleVision" 등으로 교체
    private static final double[] FAKE_CONFIDENCE_SCORES = {0.91, 0.68, 0.47}; // 실제 API처럼 신뢰도 내림차순

    private final IngredientRepository ingredientRepository;
    private final ImageRecognitionLogRepository imageRecognitionLogRepository;

    @Transactional
    public List<RecognizedIngredientResponse> recognize(Long userId, MultipartFile image) {
        // 인식 시도 자체는 항상 로그로 남김 (나중에 실제 API 붙이면 성공/실패 분석에 씀)
        imageRecognitionLogRepository.save(
                ImageRecognitionLog.builder()
                        .userId(userId)
                        .imageFileName(image.getOriginalFilename())
                        .apiProvider(STUB_PROVIDER)
                        .build()
        );

        List<Ingredient> allIngredients = new ArrayList<>(ingredientRepository.findAll());
        if (allIngredients.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(allIngredients);
        int candidateCount = Math.min(FAKE_CONFIDENCE_SCORES.length, allIngredients.size());

        List<RecognizedIngredientResponse> results = new ArrayList<>();
        for (int i = 0; i < candidateCount; i++) {
            Ingredient ingredient = allIngredients.get(i);
            results.add(new RecognizedIngredientResponse(
                    ingredient.getIngredientId(),
                    ingredient.getIngredientName(),
                    FAKE_CONFIDENCE_SCORES[i]
            ));
        }
        return results;
    }
}
