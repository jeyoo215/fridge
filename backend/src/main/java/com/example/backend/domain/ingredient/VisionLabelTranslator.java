package com.example.backend.domain.ingredient;

import org.springframework.stereotype.Component;

import java.util.Map;

// Google Vision의 라벨 인식 결과는 거의 항상 영문으로 반환되는데,
// ingredient 테이블은 한글 재료명(예: "양파")으로 저장돼 있어 그 사이를 이어주는 번역기.
// 여기서 한글 키워드로만 바꿔주고, 실제 존재하는 재료인지는 IngredientRepository 조회로 확인한다.
// 그래서 ingredient 테이블에 재료가 추가될수록 이 매핑만 함께 늘려주면 인식 범위가 넓어진다.
@Component
public class VisionLabelTranslator {

    private static final Map<String, String> ENGLISH_TO_KOREAN = Map.ofEntries(
            Map.entry("lettuce", "상추"),
            Map.entry("onion", "양파"),
            Map.entry("tofu", "두부"),
            Map.entry("bean curd", "두부"),
            Map.entry("egg", "계란"),
            Map.entry("eggs", "계란"),
            Map.entry("tomato", "토마토"),
            Map.entry("potato", "감자"),
            Map.entry("carrot", "당근"),
            Map.entry("cabbage", "양배추"),
            Map.entry("garlic", "마늘"),
            Map.entry("cucumber", "오이"),
            Map.entry("pepper", "고추"),
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
            Map.entry("rice", "쌀"),
            Map.entry("bread", "빵"),
            Map.entry("noodle", "면"),
            Map.entry("garlic bulb", "마늘")
    );

    // 매칭되는 한글 키워드가 없으면 null (음식과 무관한 라벨이거나 아직 사전에 없는 단어)
    public String toKoreanKeyword(String englishLabel) {
        return ENGLISH_TO_KOREAN.get(englishLabel.toLowerCase());
    }
}
