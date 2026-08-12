package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityPostCommentCreateRequest;
import com.example.backend.domain.community.dto.CommunityPostCommentResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostCommentService {

    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final CommunityPostRepository communityPostRepository;

    // 댓글 등록
    @Transactional
    public Long create(Long userId, Long postId, CommunityPostCommentCreateRequest request) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        CommunityPostComment comment = CommunityPostComment.builder()
                .post(post)
                .userId(userId)
                .content(request.content())
                .build();

        return communityPostCommentRepository.save(comment).getCommentId();
    }

    // 게시글의 댓글 목록 (등록순)
    public List<CommunityPostCommentResponse> getComments(Long postId) {
        return communityPostCommentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommunityPostCommentResponse::new)
                .toList();
    }

    // 댓글 삭제 (본인 댓글만 가능)
    @Transactional
    public void delete(Long userId, Long commentId) {
        CommunityPostComment comment = communityPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 댓글입니다. id=" + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        communityPostCommentRepository.delete(comment);
    }
}
