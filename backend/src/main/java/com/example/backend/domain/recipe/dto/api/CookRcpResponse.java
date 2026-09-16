package com.example.backend.domain.recipe.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

// 식약처 API 응답 최상위 구조
// { "COOKRCP01": { "total_count": "...", "row": [ ... ] } }
// @JsonIgnoreProperties(ignoreUnknown = true): JSON에 있지만 이 클래스에 없는 필드는 무시.
//   (없으면 매핑 안 된 필드를 만났을 때 파싱 에러가 남)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CookRcpResponse {

    // @JsonProperty("COOKRCP01"): JSON의 대문자 키 "COOKRCP01"을 이 변수에 매핑
    @JsonProperty("COOKRCP01")
    private Body cookRcp01;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        // total_count: 전체 레시피 건수 (문자열로 옴, 예: "1156")
        @JsonProperty("total_count")
        private String totalCount;

        // row: 레시피 목록 배열
        @JsonProperty("row")
        private List<CookRcpRow> row;
    }
}