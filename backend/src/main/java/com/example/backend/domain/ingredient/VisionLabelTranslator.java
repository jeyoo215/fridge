package com.example.backend.domain.ingredient;
 
import com.google.cloud.translate.Translate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
// Google Vision의 라벨 인식 결과는 거의 항상 영문으로 반환되는데,
// ingredient 테이블은 한글 재료명(예: "양파")으로 저장돼 있어 그 사이를 이어주는 번역기.
//
// 예전엔 40여 개 영단어를 손으로 매핑해뒀는데(사전 방식), 이제 Google Cloud Translation API로
// 실시간 번역해서 사전에 없던 단어("kimchi", "kale" 등)도 자동으로 커버되게 바꿈.
// 번역된 한글 단어가 실제 존재하는 재료인지는 여전히 IngredientRepository 조회로 확인함
// (즉 "food", "vegetable" 같은 뭉뚱그린 단어는 번역은 되지만 재료 마스터에 없어서 여전히 매칭 안 됨 — 기존과 동일한 한계).
@Component
@RequiredArgsConstructor
@Slf4j
public class VisionLabelTranslator {
 
    private final Translate translateClient;
 
    // 같은 영단어를 매번 API로 다시 번역하면 비용/속도 낭비라서, 한 번 번역한 결과는 메모리에 캐싱해둠
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();
 
    // 매칭되는 한글 키워드가 없으면 null (번역 자체가 실패했거나 API 호출 중 오류난 경우)
    public String toKoreanKeyword(String englishLabel) {
        String key = englishLabel.toLowerCase();
        return translationCache.computeIfAbsent(key, this::translate);
    }
 
    private String translate(String englishLabel) {
        try {
            Translate.TranslateOption sourceLang = Translate.TranslateOption.sourceLanguage("en");
            Translate.TranslateOption targetLang = Translate.TranslateOption.targetLanguage("ko");
            var result = translateClient.translate(englishLabel, sourceLang, targetLang);
            return result.getTranslatedText();
        } catch (Exception e) {
            // 번역 API 호출이 실패해도(네트워크 문제, 할당량 초과 등) 카메라 인식 전체가 죽으면 안 되니,
            // 이 단어 하나만 매칭 실패로 조용히 넘어감
            log.warn("[VisionLabelTranslator] '{}' 번역 실패", englishLabel, e);
            return null;
        }
    }
}
