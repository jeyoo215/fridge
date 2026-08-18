package com.example.backend.domain.challenge;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ChallengeType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeTargetIngredient> targetIngredients = new ArrayList<>();

    @Builder
    public Challenge(Long userId, LocalDate startDate, LocalDate endDate, ChallengeType type) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.status = Status.진행중;
        this.createdAt = LocalDateTime.now();
    }

    public void addTargetIngredient(Long ingredientId) {
        ChallengeTargetIngredient target = ChallengeTargetIngredient.builder()
                .challenge(this).ingredientId(ingredientId).build();
        targetIngredients.add(target);
    }

    public void markSuccess() {
        this.status = Status.성공;
    }

    public void markFailed() {
        this.status = Status.실패;
    }

    public boolean isFinishedPeriod(LocalDate today) {
        return !today.isBefore(endDate);
    }

    public enum Status {
        진행중, 성공, 실패
    }

    public enum ChallengeType {
        FRIDGE_CLEAN,       // 냉장고 파먹기
        TARGET_INGREDIENT   // 특정 재료 소진
    }
}