package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${community.media.upload-dir:uploads/community}")
    private String uploadDir;

    // 커뮤니티 게시글에 업로드된 이미지/동영상을 /media/community/** 로 그대로 서빙 (로컬 디스크 저장, 개발용)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/community/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // allowedOriginPatterns를 쓰면 와일드카드(*)를 쓸 수 있어서,
                // 팀원마다 다른 공유기 IP 대역(192.168.x.x, 172.x.x.x 등)에서 접속해도 다 허용됨
                // (개발 단계 전용 설정, 배포할 땐 실제 도메인으로 좁혀야 함)
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://192.168.*.*:5173",
                        "http://172.*.*.*:5173",
                        "http://10.*.*.*:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
