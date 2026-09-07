package com.example.backend.domain.challenge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 진행중인 챌린지가 이미 있는지 확인 (중복 시작 방지)
    Optional<Challenge> findByUserIdAndStatus(Long userId, Challenge.Status status);

    // 성공했지만 아직 사용자가 "챌린지 완수!" 화면을 확인하지 않은 챌린지가 있는지 확인.
    // 있으면 다음 챌린지를 시작 못 하게 막고(startChallenge), 계속 활성 취급으로 돌려준다(getActiveChallenge).
    // 이론상 한 명이 여러 개를 동시에 "미확인" 상태로 갖고 있을 수도 있어서(예: 이 기능이 추가되기
    // 전에 이미 성공해둔 오래된 기록들) findFirst + 최신순으로 하나만 확정해서 가져온다.
    Optional<Challenge> findFirstByUserIdAndStatusAndAcknowledgedFalseOrderByCreatedAtDesc(
            Long userId, Challenge.Status status);

    // 챌린지 히스토리 페이지네이션 조회 (정렬은 서비스에서 Pageable에 넣어서 넘김)
    Page<Challenge> findByUserId(Long userId, Pageable pageable);
}