package com.example.backend.domain.recipe;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ERD의 조합추천이력 테이블. Python 배치(ml/calculate_combo_recommendation.py)가 채워넣고
// Spring은 조회만 한다 (FR-23)
@Entity
@Table(name = "combination_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComboRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "combo_score", nullable = false)
    private Double comboScore;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}