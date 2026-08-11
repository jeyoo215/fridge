package com.example.backend.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    // 페이지네이션: 컬렉션(join fetch)과 페이징을 동시에 하면 안 되므로, 먼저 이 페이지에 해당하는 id만 뽑는다.
    @Query("SELECT p.postId FROM CommunityPost p ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsOrderByCreatedAtDesc(Pageable pageable);

    // 위에서 뽑은 id들에 대해서만 섹션까지 함께 조회 (N+1 방지)
    @Query("SELECT DISTINCT p FROM CommunityPost p LEFT JOIN FETCH p.sections WHERE p.postId IN :postIds")
    List<CommunityPost> findAllWithSectionsByPostIdIn(@Param("postIds") List<Long> postIds);

    @Query("SELECT p FROM CommunityPost p LEFT JOIN FETCH p.sections WHERE p.postId = :postId")
    Optional<CommunityPost> findByIdWithSections(@Param("postId") Long postId);
}
