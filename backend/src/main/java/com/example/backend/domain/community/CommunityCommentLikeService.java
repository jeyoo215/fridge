package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCommentLikeService {

    private final CommunityCommentLikeRepository communityCommentLikeRepository;
    private final CommunityPostCommentRepository communityPostCommentRepository;

    // 공감 토글: 이미 눌렀으면 취소, 안 눌렀으면 새로 공감 (CommunityPostLikeService와 동일한 패턴).
    // 개수는 매번 COUNT 쿼리로 세지 않고 CommunityPostComment.likeCount에 저장해서 관리한다.
    @Transactional
    public ToggleResponse toggle(Long userId, Long commentId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + commentId));

        var existing = communityCommentLikeRepository.findByComment_CommentIdAndUserId(commentId, userId);
        if (existing.isPresent()) {
            communityCommentLikeRepository.delete(existing.get());
            comment.decreaseLikeCount();
        } else {
            communityCommentLikeRepository.save(CommunityCommentLike.builder().comment(comment).userId(userId).build());
            comment.increaseLikeCount();
        }

        boolean active = existing.isEmpty();
        return new ToggleResponse(active, comment.getLikeCount());
    }
}
