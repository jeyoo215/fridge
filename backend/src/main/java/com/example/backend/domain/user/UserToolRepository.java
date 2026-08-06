package com.example.backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserToolRepository extends JpaRepository<UserTool, Long> {

    // 마이페이지에서 본인이 선택한 조리도구 목록 조회
    List<UserTool> findByUserId(Long userId);

    // 도구 선택을 통째로 다시 저장하기 전, 기존 선택을 비움 (다중선택 결과를 매번 새로 저장하는 방식)
    void deleteByUserId(Long userId);
}
