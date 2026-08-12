package com.example.backend.domain.badge;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndBadge_BadgeId(Long userId, Long badgeId);
}