package com.example.backend.domain.auth;

import com.example.backend.domain.auth.dto.LoginRequest;
import com.example.backend.domain.auth.dto.SignupRequest;
import com.example.backend.domain.auth.dto.TokenResponse;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private SignupVerificationRepository signupVerificationRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EmailService emailService;

    // 실제 암호화 로직이 필요해서(matches 검증 포함) Mock이 아니라 진짜 구현체를 사용
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, refreshTokenRepository, passwordResetTokenRepository,
                signupVerificationRepository, passwordEncoder, jwtTokenProvider, emailService);
    }

    @Test
    @DisplayName("이메일 인증을 마친 뒤 회원가입하면 유저가 저장된다")
    void signup_정상가입() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "01012345678");
        User savedUser = user(1L, "test@example.com", passwordEncoder.encode("password123"), "테스트유저");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("01012345678")).thenReturn(false);
        when(signupVerificationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(verifiedSignupVerification("test@example.com")));
        when(userRepository.existsByNickname(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long userId = authService.signup(request);

        assertThat(userId).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입하면 예외가 발생한다")
    void signup_중복이메일이면_예외() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "01012345678");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 인증을 하지 않고 회원가입하면 예외가 발생한다")
    void signup_이메일인증안했으면_예외() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "01012345678");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("01012345678")).thenReturn(false);
        when(signupVerificationRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 인증을 먼저 완료해주세요");

        verify(userRepository, never()).save(any(User.class));
    }

    private SignupVerification verifiedSignupVerification(String email) {
        SignupVerification verification = SignupVerification.builder()
                .email(email)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        verification.markVerified();
        return verification;
    }

    @Test
    @DisplayName("정상적으로 로그인하면 액세스/리프레시 토큰이 발급된다")
    void login_정상로그인() {
        String encodedPassword = passwordEncoder.encode("password123");
        User user = user(1L, "test@example.com", encodedPassword, "테스트유저");
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1_209_600_000L);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());

        TokenResponse result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 시 예외가 발생한다")
    void login_비밀번호틀리면_예외() {
        String encodedPassword = passwordEncoder.encode("password123");
        User user = user(1L, "test@example.com", encodedPassword, "테스트유저");
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void login_존재하지않는이메일이면_예외() {
        LoginRequest request = new LoginRequest("nobody@example.com", "password123");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("정상적인 리프레시 토큰이면 새 토큰 쌍을 재발급한다")
    void reissue_정상재발급() {
        String refreshToken = "valid-refresh-token";
        User user = user(1L, "test@example.com", "encoded-pw", "테스트유저");
        RefreshToken savedToken = RefreshToken.builder()
                .userId(1L).token(refreshToken).expiresAt(LocalDateTime.now().plusDays(1)).build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims(1L, "refresh"));
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.of(savedToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1_209_600_000L);

        TokenResponse result = authService.reissue(refreshToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰이면 재발급 시 예외가 발생한다")
    void reissue_유효하지않은토큰이면_예외() {
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    @DisplayName("DB에 저장된 값과 일치하지 않는 리프레시 토큰이면 재발급 시 예외가 발생한다")
    void reissue_저장된토큰과불일치하면_예외() {
        String refreshToken = "mismatched-token";
        RefreshToken savedToken = RefreshToken.builder()
                .userId(1L).token("different-stored-token").expiresAt(LocalDateTime.now().plusDays(1)).build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims(1L, "refresh"));
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.of(savedToken));

        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일치하지 않습니다");
    }

    private User user(Long id, String email, String password, String nickname) {
        User user = User.builder().email(email).password(password).nickname(nickname).build();
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private Claims claims(Long userId, String type) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("type", type);
        return io.jsonwebtoken.Jwts.claims()
                .subject(String.valueOf(userId))
                .add(claimsMap)
                .build();
    }
}