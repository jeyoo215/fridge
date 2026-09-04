package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.NicknameUpdateRequest;
import com.example.backend.domain.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 로그인한 본인의 프로필 조회/수정. JwtAuthenticationFilter가 채워주는 인증 principal(userId)만 사용하며,
// 이 프로젝트의 다른 users/me/* 엔드포인트들과 달리 userId를 쿼리파라미터로 받지 않는다
// (로그인 기능이 이미 있으므로 여기서부터는 진짜 인증된 사용자 기준으로 동작).
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal Long userId) {
        requireLoggedIn(userId);
        return userProfileService.getMyProfile(userId);
    }

    @PatchMapping("/nickname")
    public void updateNickname(@AuthenticationPrincipal Long userId,
                                @Valid @RequestBody NicknameUpdateRequest request) {
        requireLoggedIn(userId);
        userProfileService.updateNickname(userId, request.nickname());
    }

    private void requireLoggedIn(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
