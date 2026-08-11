package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {
    List<CommunityPostComment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);
}
