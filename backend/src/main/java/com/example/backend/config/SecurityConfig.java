package com.example.backend.config;

import com.example.backend.domain.auth.CustomOAuth2UserService;
import com.example.backend.domain.auth.OAuth2LoginSuccessHandler;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.OAuth2OriginCaptureFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2OriginCaptureFilter oAuth2OriginCaptureFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://192.168.*.*:5173,http://172.*.*.*:5173,http://10.*.*.*:5173}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList()
        );
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
     }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ⚠️ 순서 중요: /api/v1/recipes/* 보다 먼저 선언해야 함
                        // (안 그러면 아래 와일드카드 permitAll에 먼저 걸려서 로그인 없이 추천 API가 뚫림)
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/recommend", "/api/v1/recipes/combo-recommend").authenticated()

                        // 공용 마스터 데이터 / 비로그인도 볼 수 있는 조회는 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ingredients", "/api/v1/ingredients/categories",
                                "/api/v1/cooking-tools",
                                "/api/v1/recipes", "/api/v1/recipes/categories"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/likes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/likes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/scraps").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/cook-records").permitAll() 
                        .requestMatchers(HttpMethod.GET, "/api/v1/community/posts", "/api/v1/community/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/community/posts/*/likes", "/api/v1/community/posts/*/scraps").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/shopping-list/shared/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/shopping-list/shared/*/items/*/toggle").permitAll()

                        // 나머지는 전부 로그인 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) ->
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(oAuth2OriginCaptureFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}