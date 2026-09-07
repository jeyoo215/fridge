package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

// VisionConfig랑 같은 서비스 계정 키(google-vision-key.json)를 그대로 재사용함.
// 단, GCP 콘솔에서 이 프로젝트에 "Cloud Translation API"가 별도로 활성화되어 있어야 동작함
// (Vision API 활성화와는 별개 설정이라, 키 소유자가 콘솔에서 한 번 켜줘야 함).
@Configuration
public class TranslateConfig {

    @Value("${google.vision.credentials.location:classpath:google-vision-key.json}")
    private Resource credentialsResource;

    @Bean
    public Translate translateClient() throws IOException {
        try (InputStream credentialsStream = credentialsResource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            return TranslateOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
        }
    }
}
