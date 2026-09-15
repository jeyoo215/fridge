package com.example.backend.domain.challenge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 진행중인 챌린지가 이미 있는지 확인 (중복 시작 방지)
    Optional<Challenge> findByUserIdAndStatus(Long userId, Challenge.Status status);

    // 챌린지 히스토리 페이지네이션 조회 (정렬은 서비스에서 Pageable에 넣어서 넘김)
    Page<Challenge> findByUserId(Long userId, Pageable pageable);
}