package com.example.backend.domain.auth;

import com.example.backend.domain.auth.dto.LoginRequest;
import com.example.backend.domain.auth.dto.ReissueRequest;
import com.example.backend.domain.auth.dto.SignupRequest;
import com.example.backend.domain.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> signup(@Valid @RequestBody SignupRequest request) {
        return Map.of("userId", authService.signup(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request.refreshToken());
    }

    // JwtAuthenticationFilter가 SecurityContext에 넣어준 userId(Long)를 그대로 principal로 받음
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal Long userId) {
        if (userId != null) {
            authService.logout(userId);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}