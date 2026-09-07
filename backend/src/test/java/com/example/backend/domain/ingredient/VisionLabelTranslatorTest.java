package com.example.backend.domain.ingredient;

import com.google.cloud.translate.Translate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionLabelTranslatorTest {

    @Mock
    private Translate translateClient;

    @Test
    @DisplayName("Translation API가 실패해도(예: 프로젝트에서 비활성화) 사전에 있는 라벨이면 한글 키워드를 반환한다")
    void toKoreanKeyword_번역API실패시_사전에서_fallback() {
        when(translateClient.translate(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Cloud Translation API has not been used in project"));
        VisionLabelTranslator translator = new VisionLabelTranslator(translateClient);

        assertThat(translator.toKoreanKeyword("onion")).isEqualTo("양파");
        assertThat(translator.toKoreanKeyword("Tomato")).isEqualTo("토마토");
    }

    @Test
    @DisplayName("번역 API도 실패하고 사전에도 없는 라벨이면 null을 반환한다 (전체 인식이 죽지 않아야 함)")
    void toKoreanKeyword_번역API실패_사전에도없으면_null() {
        when(translateClient.translate(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Cloud Translation API has not been used in project"));
        VisionLabelTranslator translator = new VisionLabelTranslator(translateClient);

        assertThat(translator.toKoreanKeyword("astronomical object")).isNull();
    }
}
