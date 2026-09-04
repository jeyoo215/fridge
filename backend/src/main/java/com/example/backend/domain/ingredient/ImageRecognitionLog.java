package com.example.backend.domain.ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ERD의 image_recognition_log 테이블 (카메라 인식 시도 기록)
@Entity
@Table(name = "image_recognition_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageRecognitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "image_file_name")
    private String imageFileName;

    @Column(name = "api_provider")
    private String apiProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ImageRecognitionLog(Long userId, String imageFileName, String apiProvider) {
        this.userId = userId;
        this.imageFileName = imageFileName;
        this.apiProvider = apiProvider;
        this.createdAt = LocalDateTime.now();
    }
}
