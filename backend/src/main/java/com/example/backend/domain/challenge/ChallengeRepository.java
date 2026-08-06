package com.example.backend.domain.challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 진행중인 챌린지가 이미 있는지 확인 (중복 시작 방지)
    Optional<Challenge> findByUserIdAndStatus(Long userId, Challenge.Status status);

    List<Challenge> findByUserIdOrderByCreatedAtDesc(Long userId);
}