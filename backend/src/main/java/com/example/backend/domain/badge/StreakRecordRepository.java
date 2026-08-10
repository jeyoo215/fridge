package com.example.backend.domain.badge;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StreakRecordRepository extends JpaRepository<StreakRecord, Long> {
    Optional<StreakRecord> findByUserId(Long userId);
}