package com.example.backend.domain.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComboRecommendationRepository extends JpaRepository<ComboRecommendation, Long> {
    List<ComboRecommendation> findByUserIdOrderByComboScoreDesc(Long userId);
}