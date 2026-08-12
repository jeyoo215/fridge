package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {
    Optional<CommunityPostLike> findByPost_PostIdAndUserId(Long postId, Long userId);
    long countByPost_PostId(Long postId);

    // 마이페이지 "내 활동 > 좋아요한 게시글" 목록 (최신순)
    List<CommunityPostLike> findByUserIdOrderByCreatedAtDesc(Long userId);
}
