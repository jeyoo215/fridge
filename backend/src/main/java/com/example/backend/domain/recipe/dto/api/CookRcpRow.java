package com.example.backend.domain.recipe.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 식약처 API의 레시피 한 건 (row 배열의 각 요소)
//
// [필드명 매핑]
// 식약처 필드명은 대문자 스네이크(RCP_NM)라 자바 변수명(rcpNm)과 다름.
// @JsonProperty("RCP_NM") 이 JSON 필드 ↔ 자바 변수를 연결해 줌.
//
// [필드 무시]
// @JsonIgnoreProperties(ignoreUnknown = true): 매핑하지 않은 나머지 필드는 무시.
// 식약처 API엔 MANUAL01~20(조리순서), MANUAL_IMG01~ 등 필드 너무 많음
//
// [조리순서 관련 TODO]
// 조리순서(MANUAL01~MANUAL20) + 조리순서 이미지(MANUAL_IMG01~)는 여기 아직 안 넣음.
// 필드가 40개라 많아서, 우선 기본정보+재료원문+영양성분부터 수집하고
// CookingStep 엔티티를 채우는 단계에서 MANUAL 필드들을 추가할 예정.
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CookRcpRow {

    @JsonProperty("RCP_SEQ")
    private String rcpSeq;          // 고유번호 → Recipe.externalId

    @JsonProperty("RCP_NM")
    private String rcpNm;           // 메뉴명 → Recipe.recipeName

    @JsonProperty("RCP_PARTS_DTLS")
    private String rcpPartsDtls;    // 재료 원문 → Recipe.rawIngredients (파싱 전 원본 보존)

    @JsonProperty("RCP_PAT2")
    private String rcpPat2;         // 요리종류(반찬/국/후식...) → RecipeCategory 매핑용

    @JsonProperty("RCP_WAY2")
    private String rcpWay2;         // 조리방법(찌기/끓이기...)

    @JsonProperty("ATT_FILE_NO_MAIN")
    private String attFileNoMain;   // 대표 이미지 URL → Recipe.imageUrl

    @JsonProperty("INFO_ENG")
    private String infoEng;         // 열량 → Recipe.calorie

    @JsonProperty("INFO_CAR")
    private String infoCar;         // 탄수화물 → Recipe.carbohydrate

    @JsonProperty("INFO_PRO")
    private String infoPro;         // 단백질 → Recipe.protein

    @JsonProperty("INFO_FAT")
    private String infoFat;         // 지방 → Recipe.fat

    @JsonProperty("INFO_NA")
    private String infoNa;          // 나트륨 → Recipe.sodium


    @JsonProperty("MANUAL01") private String manual01;
    @JsonProperty("MANUAL02") private String manual02;
    @JsonProperty("MANUAL03") private String manual03;
    @JsonProperty("MANUAL04") private String manual04;
    @JsonProperty("MANUAL05") private String manual05;
    @JsonProperty("MANUAL06") private String manual06;
    @JsonProperty("MANUAL07") private String manual07;
    @JsonProperty("MANUAL08") private String manual08;
    @JsonProperty("MANUAL09") private String manual09;
    @JsonProperty("MANUAL10") private String manual10;
    @JsonProperty("MANUAL11") private String manual11;
    @JsonProperty("MANUAL12") private String manual12;
    @JsonProperty("MANUAL13") private String manual13;
    @JsonProperty("MANUAL14") private String manual14;
    @JsonProperty("MANUAL15") private String manual15;
    @JsonProperty("MANUAL16") private String manual16;
    @JsonProperty("MANUAL17") private String manual17;
    @JsonProperty("MANUAL18") private String manual18;
    @JsonProperty("MANUAL19") private String manual19;
    @JsonProperty("MANUAL20") private String manual20;

    // MANUAL01~20을 순서대로 리스트로 (빈 값 제외, 앞의 "1.", "2." 번호 제거)
    public java.util.List<String> getManuals() {
        return java.util.stream.Stream.of(
                manual01, manual02, manual03, manual04, manual05,
                manual06, manual07, manual08, manual09, manual10,
                manual11, manual12, manual13, manual14, manual15,
                manual16, manual17, manual18, manual19, manual20)
            .filter(s -> s != null && !s.isBlank())
            .map(String::trim)
            .map(s -> s.replaceFirst("^\\d+\\.?\\s*", "")) // 앞의 "1." "2 " 같은 번호 제거
            .toList();
    }
}