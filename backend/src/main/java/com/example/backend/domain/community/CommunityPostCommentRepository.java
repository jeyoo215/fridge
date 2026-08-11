package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {
    List<CommunityPostComment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    // 마이페이지 "내 활동 > 댓글 단 게시글" 목록 (최신 댓글순, 같은 글에 여러 댓글을 달았으면 서비스단에서 중복 제거)
    List<CommunityPostComment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
