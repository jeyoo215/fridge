package com.example.backend.domain.challenge;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_target_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeTargetIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Builder
    public ChallengeTargetIngredient(Challenge challenge, Long ingredientId) {
        this.challenge = challenge;
        this.ingredientId = ingredientId;
    }
}