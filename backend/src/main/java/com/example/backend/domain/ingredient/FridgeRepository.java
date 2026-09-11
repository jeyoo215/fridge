package com.example.backend.domain.ingredient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    Optional<Fridge> findByUserId(Long userId);
}
