package com.example.backend.domain.auth;

import com.example.backend.domain.auth.dto.LoginRequest;
import com.example.backend.domain.auth.dto.SignupRequest;
import com.example.backend.domain.auth.dto.TokenResponse;
import com.example.backend.domain.user.Role;
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
    @Mock private JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // AuthService가 이메일 인증/비밀번호 재설정 관련 리포지토리도 필요하지만,
        // 이 테스트들에서는 안 쓰이니 null로 채워도 무방 (실제 사용 시 NPE 나면 그 테스트만 문제)
        authService = new AuthService(
                userRepository, refreshTokenRepository, null, null,
                passwordEncoder, jwtTokenProvider, null
        );
    }

    @Test
    @DisplayName("정상적으로 로그인하면 새 세션(리프레시 토큰)이 저장된다")
    void login_정상로그인() {
        String encodedPassword = passwordEncoder.encode("password123");
        User user = user(1L, "test@example.com", encodedPassword, "테스트유저");
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com", Role.USER)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1_209_600_000L);

        TokenResponse result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class)); // 갱신이 아니라 매번 새로 저장
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
    @DisplayName("정상적인 리프레시 토큰이면 같은 세션(row)을 회전시켜 재발급한다")
    void reissue_정상재발급() {
        String refreshToken = "valid-refresh-token";
        User user = user(1L, "test@example.com", "encoded-pw", "테스트유저");
        RefreshToken savedToken = RefreshToken.builder()
                .userId(1L).token(refreshToken).expiresAt(LocalDateTime.now().plusDays(1)).build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims(1L, "refresh"));
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(savedToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com", Role.USER)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1_209_600_000L);

        TokenResponse result = authService.reissue(refreshToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(savedToken.getToken()).isEqualTo("new-refresh-token"); // 같은 row가 회전됐는지 확인
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
    @DisplayName("DB에 해당 토큰의 세션이 없으면 재발급 시 예외가 발생한다")
    void reissue_세션없으면_예외() {
        String refreshToken = "not-found-token";

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims(1L, "refresh"));
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("로그아웃되었거나 존재하지 않는 세션");
    }

    @Test
    @DisplayName("만료된 세션이면 재발급 시 예외가 발생하고 row가 삭제된다")
    void reissue_만료된세션이면_예외() {
        String refreshToken = "expired-token";
        RefreshToken savedToken = RefreshToken.builder()
                .userId(1L).token(refreshToken).expiresAt(LocalDateTime.now().minusDays(1)).build();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims(1L, "refresh"));
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(savedToken));

        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료");

        verify(refreshTokenRepository).delete(savedToken);
    }

    private User user(Long id, String email, String password, String nickname) {
        User user = User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
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