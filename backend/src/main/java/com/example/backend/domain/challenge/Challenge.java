package com.example.backend.domain.challenge;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ERD의 challenge 테이블 (냉장고 파먹기 챌린지, FR-40)
@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Challenge(Long userId, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = Status.진행중;
        this.createdAt = LocalDateTime.now();
    }

    public void markSuccess() {
        this.status = Status.성공;
    }

    public void markFailed() {
        this.status = Status.실패;
    }

    public boolean isFinishedPeriod(LocalDate today) {
        return !today.isBefore(endDate); // today >= endDate
    }

    public enum Status {
        진행중, 성공, 실패
    }
}