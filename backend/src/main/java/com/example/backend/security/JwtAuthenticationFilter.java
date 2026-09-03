package com.example.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        log.info("[JWT필터] URI={}, token존재={}", request.getRequestURI(), token != null);

        if (token != null) {
            try {
                var claims = jwtTokenProvider.parseClaims(token);
                log.info("[JWT필터] claims type={}, role={}", claims.get("type"), claims.get("role"));
                if ("access".equals(claims.get("type"))) {
                    Long userId = Long.valueOf(claims.getSubject());
                    String role = claims.get("role", String.class);
                    var authorities = role != null
                            ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            : List.<SimpleGrantedAuthority>of();

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[JWT필터] 인증 설정 완료: userId={}, authorities={}", userId, authorities);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.error("[JWT필터] 토큰 파싱 실패", e);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);

        // 필터체인 끝난 뒤 최종 인증 상태 확인 (다른 필터가 지웠는지 확인용)
        var finalAuth = SecurityContextHolder.getContext().getAuthentication();
        log.info("[JWT필터] 응답 시점 최종 인증 상태: {}", finalAuth);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}