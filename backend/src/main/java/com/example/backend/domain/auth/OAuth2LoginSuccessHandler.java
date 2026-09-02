package com.example.backend.domain.auth;

import com.example.backend.domain.user.Role;
import com.example.backend.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

// 카카오 로그인 완료 후 우리 JWT(access/refresh)를 발급해서 프론트로 리다이렉트시키는 핸들러
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = (Long) oAuth2User.getAttributes().get("userId");
        String email = (String) oAuth2User.getAttributes().getOrDefault("email", "");
        Role role = (Role) oAuth2User.getAttributes().get("role");

        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 트랜잭션이 확실히 걸린 서비스에 저장을 위임 (self-invocation으로 인한 트랜잭션 미적용 방지)
        refreshTokenService.saveOrUpdate(userId, refreshToken, jwtTokenProvider.getRefreshTokenExpirationMs());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}