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

    // Cloud Translation API가 GCP 프로젝트에서 비활성화/할당량 초과 등으로 막혀있을 때를 대비한 예전 방식(수동 사전).
    // ingredient 마스터의 재료명과 자주 매칭되는 라벨들만 우선 채워둠 — API가 정상화되면 이 사전은 fallback으로만 쓰인다.
    private static final Map<String, String> FALLBACK_DICTIONARY = Map.ofEntries(
            Map.entry("lettuce", "상추"),
            Map.entry("onion", "양파"),
            Map.entry("tomato", "토마토"),
            Map.entry("cherry tomato", "토마토"),
            Map.entry("potato", "감자"),
            Map.entry("carrot", "당근"),
            Map.entry("cabbage", "양배추"),
            Map.entry("garlic", "마늘"),
            Map.entry("cucumber", "오이"),
            Map.entry("chili pepper", "고추"),
            Map.entry("bell pepper", "피망"),
            Map.entry("mushroom", "버섯"),
            Map.entry("spinach", "시금치"),
            Map.entry("broccoli", "브로콜리"),
            Map.entry("apple", "사과"),
            Map.entry("banana", "바나나"),
            Map.entry("orange", "오렌지"),
            Map.entry("lemon", "레몬"),
            Map.entry("grape", "포도"),
            Map.entry("strawberry", "딸기"),
            Map.entry("watermelon", "수박"),
            Map.entry("milk", "우유"),
            Map.entry("cheese", "치즈"),
            Map.entry("yogurt", "요거트"),
            Map.entry("butter", "버터"),
            Map.entry("beef", "소고기"),
            Map.entry("pork", "돼지고기"),
            Map.entry("chicken", "닭고기"),
            Map.entry("sausage", "소시지"),
            Map.entry("bacon", "베이컨"),
            Map.entry("fish", "생선"),
            Map.entry("shrimp", "새우"),
            Map.entry("squid", "오징어"),
            Map.entry("crab", "게"),
            Map.entry("tofu", "두부"),
            Map.entry("egg", "계란"),
            Map.entry("rice", "쌀"),
            Map.entry("bread", "빵"),
            Map.entry("noodle", "면"),
            Map.entry("scallion", "대파"),
            Map.entry("green onion", "대파"),
            Map.entry("chives", "부추"),
            Map.entry("zucchini", "애호박"),
            Map.entry("eggplant", "가지"),
            Map.entry("bean sprout", "콩나물"),
            Map.entry("seaweed", "김")
    );

    private final Translate translateClient;

    // 같은 영단어를 매번 API로 다시 번역하면 비용/속도 낭비라서, 한 번 번역한 결과는 메모리에 캐싱해둠
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    // 매칭되는 한글 키워드가 없으면 null (번역 자체가 실패했고 사전에도 없는 경우)
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
            // 번역 API 호출이 실패해도(네트워크 문제, 할당량 초과, API 비활성화 등) 카메라 인식 전체가 죽으면 안 되니,
            // 수동 사전으로 한 번 더 시도해보고, 그래도 없으면 이 단어만 매칭 실패로 조용히 넘어감
            String fallback = FALLBACK_DICTIONARY.get(englishLabel);
            log.warn("[VisionLabelTranslator] '{}' 번역 API 실패, 사전 fallback {}", englishLabel,
                    fallback != null ? "'" + fallback + "' 사용" : "에도 없음", e);
            return fallback;
        }
    }
}
