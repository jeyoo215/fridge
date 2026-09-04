package com.example.backend.domain.recipe.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Claude가 파싱해서 돌려주는 재료 1건
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedIngredient {
    private String name;         // 재료명 (정규화된 표준형)

    @JsonProperty("isSeasoning") // JSON 키 명시 (안 하면 Jackson이 "seasoning"으로 찾아서 매핑 실패)
    private boolean isSeasoning; // 조미료 여부

    private Double quantity;     // 분량 (없으면 null)
    private String unit;         // 단위 (없으면 null)
}