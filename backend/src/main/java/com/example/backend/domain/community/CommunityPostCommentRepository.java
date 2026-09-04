package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {
    List<CommunityPostComment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    // 마이페이지 "내 활동 > 댓글 단 게시글" 목록 (최신 댓글순, 같은 글에 여러 댓글을 달았으면 서비스단에서 중복 제거)
    List<CommunityPostComment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 게시글 삭제 시 먼저 호출: FK 제약 때문에 댓글을 먼저 지워야 게시글을 지울 수 있음
    void deleteByPost_PostId(Long postId);

    // 댓글(원댓글/대댓글) 삭제 시 그 밑에 달린 답글들을 재귀적으로 찾아 같이 지우고, 딸린 신고도
    // 정리하기 위함 (CommunityPostCommentService.deleteCommentInternal)
    List<CommunityPostComment> findByParentCommentId(Long parentCommentId);
}
