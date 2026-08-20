package com.example.backend.domain.auth;

import com.example.backend.domain.user.AuthProvider;
import com.example.backend.domain.user.User;
import com.example.backend.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("카카오 사용자 정보 응답: {}", attributes); // 실제 응답 구조 확인용

            String providerId = String.valueOf(attributes.get("id"));

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile")
                    : null;

            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            if (nickname == null || nickname.isBlank()) {
                nickname = "카카오사용자" + providerId.substring(Math.max(0, providerId.length() - 4));
            }

            String finalNickname = nickname;
            User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, providerId)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .email(email != null ? email : "kakao_" + providerId + "@no-email.fridge")
                                    .password(null)
                                    .nickname(finalNickname)
                                    .provider(AuthProvider.KAKAO)
                                    .providerId(providerId)
                                    .build()
                    ));

            Map<String, Object> customAttributes = new HashMap<>(attributes);
            customAttributes.put("userId", user.getUserId());
            customAttributes.put("email", user.getEmail());

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    customAttributes,
                    "id"
            );
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 예외 발생", e); // 무슨 예외든 여기서 반드시 로그 찍힘
            throw e;
        }
    }
}