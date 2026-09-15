package com.example.backend.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class VisionConfig {

    @Value("${google.vision.credentials.location:classpath:google-vision-key.json}")
    private Resource credentialsResource;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try (InputStream credentialsStream = credentialsResource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            return ImageAnnotatorClient.create(settings);
        }
    }
}
