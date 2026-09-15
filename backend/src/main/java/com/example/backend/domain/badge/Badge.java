package com.example.backend.domain.badge;

import jakarta.persistence.*;
import lombok.*;

// 뱃지 마스터 데이터 (FR-42)
@Entity
@Table(name = "badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "badge_name", nullable = false, length = 50, unique = true)
    private String badgeName;

    @Column(nullable = false, length = 200)
    private String description;

    // 뱃지 획득 조건 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, columnDefinition = "VARCHAR(30)")
    private ConditionType conditionType;

    // 조건 충족 기준값 (예: STREAK_COUNT + 3 => 3연속 성공 시 지급)
    @Column(name = "condition_value", nullable = false)
    private Integer conditionValue;

    @Builder
    public Badge(String badgeName, String description, ConditionType conditionType, Integer conditionValue) {
        this.badgeName = badgeName;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    public enum ConditionType {
        CHALLENGE_SUCCESS_COUNT, // 누적 챌린지 성공 횟수
        STREAK_COUNT             // 연속 성공 스트릭
    }
}