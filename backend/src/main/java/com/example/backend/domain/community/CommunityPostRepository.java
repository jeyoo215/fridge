package com.example.backend.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    // 페이지네이션: 컬렉션(join fetch)과 페이징을 동시에 하면 안 되므로, 먼저 이 페이지에 해당하는 id만 뽑는다.
    // data.sql이 재시작마다 boardType=null인 예전 글을 RECIPE로 백필해두므로, 여기서는 boardType이
    // 항상 채워져 있다고 가정하고 단순 equals 비교만 한다.
    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeOrderByCreatedAtDesc(@Param("boardType") CommunityPost.BoardType boardType, Pageable pageable);

    // 인기순(좋아요 많은 순) 정렬. likeCount가 CommunityPost에 저장되어 있어 집계 없이 바로 정렬 가능.
    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeOrderByLikeCountDesc(@Param("boardType") CommunityPost.BoardType boardType, Pageable pageable);

    // 전체 잡담 게시판(FREE_TALK) 전용: 말머리로도 필터링
    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType AND p.prefix = :prefix ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeAndPrefixOrderByCreatedAtDesc(@Param("boardType") CommunityPost.BoardType boardType,
                                                                    @Param("prefix") String prefix, Pageable pageable);

    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType AND p.prefix = :prefix ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeAndPrefixOrderByLikeCountDesc(@Param("boardType") CommunityPost.BoardType boardType,
                                                                    @Param("prefix") String prefix, Pageable pageable);

    // 제목 검색. 검색어가 있으면 말머리 필터보다 우선한다(둘 다 동시에 적용하지 않음).
    // 띄어쓰기 차이로 "감자 주스"가 "감자주스"를 못 찾는 문제를 막기 위해, 저장된 제목과 검색어 둘 다
    // 공백을 지우고 비교한다(검색어 쪽 공백 제거는 CommunityPostService에서 미리 해둠).
    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType AND REPLACE(p.title, ' ', '') LIKE CONCAT('%', :keyword, '%') ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeAndTitleContainingOrderByCreatedAtDesc(@Param("boardType") CommunityPost.BoardType boardType,
                                                                             @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p.postId FROM CommunityPost p WHERE p.boardType = :boardType AND REPLACE(p.title, ' ', '') LIKE CONCAT('%', :keyword, '%') ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Long> findPostIdsByBoardTypeAndTitleContainingOrderByLikeCountDesc(@Param("boardType") CommunityPost.BoardType boardType,
                                                                             @Param("keyword") String keyword, Pageable pageable);

    // 위에서 뽑은 id들에 대해서만 조리순서까지 함께 조회 (목록 카드 썸네일/미리보기용, N+1 방지)
    @Query("SELECT DISTINCT p FROM CommunityPost p LEFT JOIN FETCH p.steps WHERE p.postId IN :postIds")
    List<CommunityPost> findAllWithStepsByPostIdIn(@Param("postIds") List<Long> postIds);
}
