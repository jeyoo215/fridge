package com.example.backend.domain.auth;

import com.example.backend.domain.auth.dto.LoginRequest;
import com.example.backend.domain.auth.dto.SignupRequest;
import com.example.backend.domain.auth.dto.TokenResponse;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RESET_CODE_EXPIRATION_MINUTES = 10;
    private static final int SIGNUP_CODE_EXPIRATION_MINUTES = 10;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SignupVerificationRepository signupVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    // 프론트에서 "인증코드는 n분간 유효합니다" 같은 안내 문구를 하드코딩하지 않고 서버 값 그대로 보여주기 위함
    public int getSignupCodeExpirationMinutes() {
        return SIGNUP_CODE_EXPIRATION_MINUTES;
    }

    public int getResetCodeExpirationMinutes() {
        return RESET_CODE_EXPIRATION_MINUTES;
    }

    // 실시간 이메일 중복 확인용 (회원가입 폼에서 인증코드 받기 전에 먼저 확인)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // 회원가입 이메일 인증 1단계: 인증 코드 발급 + 발송
    @Transactional
    public void sendSignupVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(SIGNUP_CODE_EXPIRATION_MINUTES);

        signupVerificationRepository.findByEmail(email)
                .ifPresentOrElse(
                        existing -> existing.update(code, expiresAt),
                        () -> signupVerificationRepository.save(
                                SignupVerification.builder()
                                        .email(email)
                                        .code(code)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );

        emailService.sendVerificationCode(email, code, "회원가입");
        log.info("[SignupVerification] {}로 인증 코드 이메일 발송 완료 ({}분 후 만료)", email, SIGNUP_CODE_EXPIRATION_MINUTES);
    }

    // 회원가입 이메일 인증 2단계: 코드 확인
    @Transactional
    public void verifySignupCode(String email, String code) {
        SignupVerification verification = signupVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 먼저 요청해주세요."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        verification.markVerified();
    }

    // 회원가입 3단계: 이메일 인증을 완료한 이력이 있어야만 가입 처리됨.
    // 닉네임은 받지 않는다. nickname 컬럼이 NOT NULL이라 임시값을 우선 채워두는 것뿐이고,
    // 프론트(Login.jsx)가 가입 직후 로그인 처리를 마치자마자 "닉네임을 지어주세요" 화면으로 보내서
    // PATCH /users/me/nickname으로 바로 덮어쓰게 한다 — 유저가 직접 짓지 않은 닉네임이 화면에 노출될 일은 없음.
    @Transactional
    public Long signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
        }

        SignupVerification verification = signupVerificationRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증을 먼저 완료해주세요."));
        if (!verification.isVerified() || verification.isExpired()) {
            throw new IllegalArgumentException("이메일 인증을 먼저 완료해주세요.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(generatePlaceholderNickname())
                .phone(request.phone())
                .build();

        return userRepository.save(user).getUserId();
    }

    // nickname에 unique 제약이 있어서, 임시값끼리도 겹치면 저장이 실패함 — 안 겹칠 때까지 다시 뽑음.
    private String generatePlaceholderNickname() {
        String nickname;
        do {
            nickname = "새싹유저" + (1000 + RANDOM.nextInt(9000));
        } while (userRepository.existsByNickname(nickname));
        return nickname;
    }

    // 아이디(이메일) 찾기: 회원가입 때 입력한 휴대폰번호가 정확히 일치해야 함
    public String findEmail(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));
        return maskEmail(user.getEmail());
    }

    // 비밀번호 재설정 1단계: 인증 코드 발급 + 이메일로 실제 발송 (Gmail SMTP, EmailService 참고: disastermonitor 프로젝트)
    @Transactional
    public void requestPasswordReset(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_CODE_EXPIRATION_MINUTES);

        passwordResetTokenRepository.findByEmail(email)
                .ifPresentOrElse(
                        existing -> existing.update(code, expiresAt),
                        () -> passwordResetTokenRepository.save(
                                PasswordResetToken.builder()
                                        .email(email)
                                        .code(code)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );

        emailService.sendVerificationCode(email, code, "비밀번호 재설정");
        log.info("[PasswordReset] {}로 인증 코드 이메일 발송 완료 ({}분 후 만료)", email, RESET_CODE_EXPIRATION_MINUTES);
    }

    // 비밀번호 재설정 2단계: 코드 확인만 함 (여기서 검증돼야 프론트에서 새 비밀번호 입력칸이 열림)
    @Transactional
    public void verifyPasswordResetCode(String email, String code) {
        PasswordResetToken token = passwordResetTokenRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 먼저 요청해주세요."));

        if (token.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!token.getCode().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        token.markVerified();
    }

    // 비밀번호 재설정 3단계: 코드 인증을 완료한 이력이 있어야만 새 비밀번호 적용됨
    @Transactional
    public void confirmPasswordReset(String email, String code, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증을 먼저 요청해주세요."));

        if (token.isUsed() || token.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!token.isVerified() || !token.getCode().equals(code)) {
            throw new IllegalArgumentException("이메일 인증을 먼저 완료해주세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.updatePassword(passwordEncoder.encode(newPassword));
        token.markUsed();
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    // 이메일 앞부분 일부만 보여주고 나머지는 가림 (예: ab****@example.com)
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) {
            return email;
        }
        String visible = email.substring(0, 2);
        String masked = "*".repeat(at - 2);
        return visible + masked + email.substring(at);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        var claims = jwtTokenProvider.parseClaims(refreshToken);
        if (!"refresh".equals(claims.get("type"))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        Long userId = Long.valueOf(claims.getSubject());

        RefreshToken saved = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그아웃되었거나 존재하지 않는 세션입니다."));

        if (!saved.getToken().equals(refreshToken) || saved.isExpired()) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었거나 일치하지 않습니다. 다시 로그인해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000);

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(user.getUserId())
                                        .token(refreshToken)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );

        return new TokenResponse(accessToken, refreshToken);
    }
}