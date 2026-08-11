package com.example.backend.domain.community;

import com.example.backend.domain.social.dto.ToggleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostScrapService {

    private final CommunityPostScrapRepository communityPostScrapRepository;
    private final CommunityPostRepository communityPostRepository;

    // 스크랩 토글: 이미 해뒀으면 취소, 안 해뒀으면 새로 스크랩 (RecipeScrapService와 동일한 패턴)
    @Transactional
    public ToggleResponse toggle(Long userId, Long postId) {
        var existing = communityPostScrapRepository.findByPost_PostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            communityPostScrapRepository.delete(existing.get());
        } else {
            CommunityPost post = communityPostRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));
            communityPostScrapRepository.save(CommunityPostScrap.builder().post(post).userId(userId).build());
        }

        long count = communityPostScrapRepository.countByPost_PostId(postId);
        boolean active = existing.isEmpty();
        return new ToggleResponse(active, count);
    }

    // 지금 스크랩 상태 + 총 개수 조회
    public ToggleResponse getStatus(Long userId, Long postId) {
        boolean active = communityPostScrapRepository.findByPost_PostIdAndUserId(postId, userId).isPresent();
        long count = communityPostScrapRepository.countByPost_PostId(postId);
        return new ToggleResponse(active, count);
    }
}
