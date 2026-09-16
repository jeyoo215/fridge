package com.example.backend.domain.community;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommunityNotificationRepository extends JpaRepository<CommunityNotification, Long> {
    // 알림 벨 드롭다운 목록 (최신순). 무한정 쌓이는 걸 막기 위해 서비스단에서 Pageable로 개수를 제한한다.
    List<CommunityNotification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndReadFalse(Long recipientUserId);

    // "모두 읽음" 처리 — 안 읽은 것만 골라 한 번의 UPDATE 쿼리로 처리 (건별로 불러와서 저장하지 않음)
    @Modifying
    @Query("update CommunityNotification n set n.read = true where n.recipientUserId = :userId and n.read = false")
    void markAllReadByRecipientUserId(@Param("userId") Long userId);

    // 게시글 삭제 시 그 글에 대한 알림도 같이 정리 (CommunityPostService.deletePostInternal)
    void deleteByPostId(Long postId);

    // 댓글(들) 삭제 시 그 댓글을 가리키던 알림도 같이 정리 (CommunityPostCommentService.deleteCommentInternal)
    void deleteByCommentIdIn(Collection<Long> commentIds);
}
