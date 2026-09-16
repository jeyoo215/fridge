package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

// 카카오 로그인을 시작한 프론트 주소(Referer)를 세션에 잠깐 저장해둔다.
// 로그인이 끝나면 OAuth2LoginSuccessHandler가 이 값을 꺼내서 "로그인을 시작했던 그 화면"으로
// 정확히 돌려보낸다 — 로컬/배포 환경마다 redirect-uri를 수동으로 다르게 설정할 필요가 없어짐.
@Slf4j
@Component
public class OAuth2OriginCaptureFilter extends OncePerRequestFilter {

    public static final String SESSION_KEY = "OAUTH2_FRONTEND_ORIGIN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/oauth2/authorization/")) {
            String referer = request.getHeader("Referer");
            log.info("[OAuth Origin 캡처] URI={}, Referer={}", request.getRequestURI(), referer);
            if (referer != null) {
                try {
                    URI uri = URI.create(referer);
                    String origin = uri.getScheme() + "://" + uri.getAuthority(); // 예: http://localhost:5173
                    HttpSession session = request.getSession(true);
                    session.setAttribute(SESSION_KEY, origin);
                } catch (IllegalArgumentException ignored) {
                    // Referer가 이상한 형식이면 그냥 무시하고 기본값(설정 파일 값)을 쓰게 둠
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}