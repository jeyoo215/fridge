package com.example.backend.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    // 페이지네이션: 컬렉션(join fetch)과 페이징을 동시에 하면 안 되므로, 먼저 이 페이지에 해당하는 id만 뽑는다.
    @Query("SELECT p.postId FROM CommunityPost p ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsOrderByCreatedAtDesc(Pageable pageable);

    // 인기순(좋아요 많은 순) 정렬. likeCount가 CommunityPost에 저장되어 있어 집계 없이 바로 정렬 가능.
    @Query("SELECT p.postId FROM CommunityPost p ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Long> findPostIdsOrderByLikeCountDesc(Pageable pageable);

    // 위에서 뽑은 id들에 대해서만 조리순서까지 함께 조회 (목록 카드 썸네일/미리보기용, N+1 방지)
    @Query("SELECT DISTINCT p FROM CommunityPost p LEFT JOIN FETCH p.steps WHERE p.postId IN :postIds")
    List<CommunityPost> findAllWithStepsByPostIdIn(@Param("postIds") List<Long> postIds);
}
