package com.example.backend.domain.auth;

import com.example.backend.domain.user.Role;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.OAuth2OriginCaptureFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    // 세션에 캡처된 origin이 없을 때만 쓰는 최종 fallback (평소엔 안 쓰일 값)
    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth/redirect}")
    private String fallbackRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = (Long) oAuth2User.getAttributes().get("userId");
        String email = (String) oAuth2User.getAttributes().getOrDefault("email", "");
        Role role = (Role) oAuth2User.getAttributes().get("role");

        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenService.saveOrUpdate(userId, refreshToken, jwtTokenProvider.getRefreshTokenExpirationMs());

        String redirectBase = resolveRedirectBase(request);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectBase + "/oauth/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .encode()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveRedirectBase(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object origin = session.getAttribute(OAuth2OriginCaptureFilter.SESSION_KEY);
            log.info("[OAuth 리다이렉트] 세션={}, 캡처된 origin={}", session, origin);
            if (origin instanceof String originStr && !originStr.isBlank()) {
                session.removeAttribute(OAuth2OriginCaptureFilter.SESSION_KEY);
                return originStr;
            }
        }
        // 세션에 캡처된 값이 없으면(예: Referer 헤더 없는 특이 케이스) 설정 파일 값으로 대체
        return fallbackRedirectUri.replace("/oauth/redirect", "");
    }
}