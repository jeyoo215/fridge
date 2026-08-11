package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostScrapRepository extends JpaRepository<CommunityPostScrap, Long> {
    Optional<CommunityPostScrap> findByPost_PostIdAndUserId(Long postId, Long userId);
    long countByPost_PostId(Long postId);
}
