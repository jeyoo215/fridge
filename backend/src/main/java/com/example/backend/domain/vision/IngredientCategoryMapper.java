package com.example.backend.domain.vision;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Vision API가 반환하는 영문 라벨을 우리 서비스의 재료 카테고리(한글)로 매핑한다.
// ingredient_category 테이블과 별개로 운영되는 임시 키워드 매핑으로,
// 추후 ingredient 마스터 데이터가 이 브랜치에 들어오면 DB 기반 매핑으로 교체한다.
@Component
public class IngredientCategoryMapper {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put("채소", List.of(
                "vegetable", "lettuce", "onion", "carrot", "cabbage", "potato", "tomato",
                "cucumber", "pepper", "garlic", "broccoli", "spinach", "mushroom", "leaf vegetable"));
        CATEGORY_KEYWORDS.put("과일", List.of(
                "fruit", "apple", "banana", "orange", "grape", "strawberry", "watermelon",
                "melon", "lemon", "peach", "pear", "citrus", "berry"));
        CATEGORY_KEYWORDS.put("육류", List.of(
                "meat", "beef", "pork", "chicken", "sausage", "bacon", "ham", "poultry"));
        CATEGORY_KEYWORDS.put("수산물", List.of(
                "fish", "seafood", "shrimp", "salmon", "tuna", "squid", "crab", "shellfish", "prawn"));
        CATEGORY_KEYWORDS.put("유제품", List.of(
                "milk", "cheese", "yogurt", "butter", "cream", "egg", "dairy"));
        CATEGORY_KEYWORDS.put("곡물", List.of(
                "rice", "bread", "noodle", "pasta", "flour", "cereal", "grain", "bun"));
        CATEGORY_KEYWORDS.put("음료", List.of(
                "beverage", "juice", "drink", "soda", "bottle", "soft drink"));
    }

    private static final Set<String> GENERIC_FOOD_KEYWORDS = Set.of(
            "food", "produce", "ingredient", "natural foods", "whole food", "dish", "cuisine", "meal", "recipe");

    // 매칭되는 카테고리가 없으면 null을 반환한다(음식과 무관한 라벨로 판단해 결과에서 제외).
    public String mapToCategory(String label) {
        String normalized = label.toLowerCase();

        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalized.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        for (String keyword : GENERIC_FOOD_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return "기타";
            }
        }

        return null;
    }
}
