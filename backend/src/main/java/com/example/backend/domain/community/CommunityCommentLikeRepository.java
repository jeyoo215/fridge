package com.example.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {
    Optional<CommunityCommentLike> findByComment_CommentIdAndUserId(Long commentId, Long userId);

    // 댓글 목록 화면에서 "내가 공감한 댓글"을 한 번에 알아내기 위한 배치 조회
    // (댓글마다 따로 조회하면 N+1이 나서, commentId 목록을 통째로 넘겨 IN 절로 한 번에 가져온다).
    @Query("select l.comment.commentId from CommunityCommentLike l where l.userId = :userId and l.comment.commentId in :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Collection<Long> commentIds);

    // 댓글(들) 삭제 시 먼저 호출: FK 제약 때문에 공감 row를 먼저 지워야 댓글을 지울 수 있음
    void deleteByComment_CommentIdIn(Collection<Long> commentIds);
}
