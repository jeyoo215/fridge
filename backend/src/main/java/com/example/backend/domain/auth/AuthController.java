package com.example.backend.domain.auth;

import com.example.backend.domain.auth.dto.FindEmailRequest;
import com.example.backend.domain.auth.dto.LoginRequest;
import com.example.backend.domain.auth.dto.LogoutRequest;
import com.example.backend.domain.auth.dto.PasswordResetConfirmRequest;
import com.example.backend.domain.auth.dto.PasswordResetRequestRequest;
import com.example.backend.domain.auth.dto.ReissueRequest;
import com.example.backend.domain.auth.dto.SendSignupCodeRequest;
import com.example.backend.domain.auth.dto.SignupRequest;
import com.example.backend.domain.auth.dto.TokenResponse;
import com.example.backend.domain.auth.dto.VerifyPasswordResetCodeRequest;
import com.example.backend.domain.auth.dto.VerifySignupCodeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일 실시간 중복 확인
    // 예: GET /api/v1/auth/check-email?email=test@example.com
    @GetMapping("/check-email")
    public Map<String, Boolean> checkEmail(@RequestParam("email") String email) {
        return Map.of("available", authService.isEmailAvailable(email));
    }

    // 회원가입 이메일 인증 1단계: 인증 코드 발급 + 발송
    // 예: POST /api/v1/auth/signup/send-code
    @PostMapping("/signup/send-code")
    public Map<String, Object> sendSignupCode(@Valid @RequestBody SendSignupCodeRequest request) {
        authService.sendSignupVerificationCode(request.email());
        return Map.of(
                "message", "인증 코드를 이메일로 보냈습니다.",
                "expiresInMinutes", authService.getSignupCodeExpirationMinutes()
        );
    }

    // 회원가입 이메일 인증 2단계: 코드 확인
    // 예: POST /api/v1/auth/signup/verify-code
    @PostMapping("/signup/verify-code")
    public Map<String, String> verifySignupCode(@Valid @RequestBody VerifySignupCodeRequest request) {
        authService.verifySignupCode(request.email(), request.code());
        return Map.of("message", "이메일 인증이 완료되었습니다.");
    }

    // 회원가입 3단계 (이메일 인증 완료 후에만 성공)
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

    // 아이디(이메일) 찾기: 회원가입 때 등록한 휴대폰번호 일치 확인
    // 예: POST /api/v1/auth/find-email
    @PostMapping("/find-email")
    public Map<String, String> findEmail(@Valid @RequestBody FindEmailRequest request) {
        return Map.of("email", authService.findEmail(request.phone()));
    }

    // 비밀번호 재설정 1단계: 인증 코드 발급 + 이메일 발송
    // 예: POST /api/v1/auth/password-reset/request
    @PostMapping("/password-reset/request")
    public Map<String, Object> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        authService.requestPasswordReset(request.email());
        return Map.of(
                "message", "인증 코드를 이메일로 보냈습니다.",
                "expiresInMinutes", authService.getResetCodeExpirationMinutes()
        );
    }

    // 비밀번호 재설정 2단계: 인증 코드 확인만 함 (성공해야 프론트에서 새 비밀번호 입력칸이 열림)
    // 예: POST /api/v1/auth/password-reset/verify-code
    @PostMapping("/password-reset/verify-code")
    public Map<String, String> verifyPasswordResetCode(@Valid @RequestBody VerifyPasswordResetCodeRequest request) {
        authService.verifyPasswordResetCode(request.email(), request.code());
        return Map.of("message", "인증이 완료되었습니다.");
    }

    // 비밀번호 재설정 3단계: 코드 인증 완료 후 새 비밀번호 적용
    // 예: POST /api/v1/auth/password-reset/confirm
    @PostMapping("/password-reset/confirm")
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.email(), request.code(), request.newPassword());
    }

    // JwtAuthenticationFilter가 SecurityContext에 넣어준 userId(Long)를 그대로 principal로 받음
    @PostMapping("/logout")
    public void logout(@RequestBody(required = false) LogoutRequest request) {
        if (request != null) {
            authService.logout(request.refreshToken());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}