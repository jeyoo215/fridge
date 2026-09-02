package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {
    List<CommunityPostComment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    // 마이페이지 "내 활동 > 댓글 단 게시글" 목록 (최신 댓글순, 같은 글에 여러 댓글을 달았으면 서비스단에서 중복 제거)
    List<CommunityPostComment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 게시글 삭제 시 먼저 호출: FK 제약 때문에 댓글을 먼저 지워야 게시글을 지울 수 있음
    void deleteByPost_PostId(Long postId);

    // 원댓글 삭제 시 그 밑의 대댓글들도 같이 지우기 위함 (CommunityPostCommentService.delete)
    void deleteByParentCommentId(Long parentCommentId);

    // 원댓글 삭제 전에, 같이 지워질 대댓글들의 id를 먼저 알아내서 그 대댓글들에 쌓인 신고도 정리하기 위함
    List<CommunityPostComment> findByParentCommentId(Long parentCommentId);
}
