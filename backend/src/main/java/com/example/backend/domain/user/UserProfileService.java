package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        return new UserProfileResponse(findUser(userId));
    }

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        userRepository.findByNickname(nickname).ifPresent(existing -> {
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        });
        findUser(userId).updateNickname(nickname);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
