package com.example.backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByPhone(String phone);
    boolean existsByNickname(String nickname);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}