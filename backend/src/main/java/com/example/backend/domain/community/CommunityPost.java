package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 레시피를 소개하는 커뮤니티 게시글 (제목 + 여러 개의 소제목/본문/이미지 섹션으로 구성되는 블로그 형태)
@Entity
@Table(name = "community_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sectionOrder ASC")
    private final List<CommunityPostSection> sections = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CommunityPost(Long userId, String title) {
        this.userId = userId;
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    public void addSection(String subtitle, String content, String mediaUrl, CommunityPostSection.MediaType mediaType) {
        CommunityPostSection section = CommunityPostSection.builder()
                .post(this)
                .sectionOrder(sections.size())
                .subtitle(subtitle)
                .content(content)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .build();
        sections.add(section);
    }

    // 게시글 수정: 제목/섹션을 통째로 새 내용으로 교체 (orphanRemoval=true라 비워지는 순간 기존 섹션은 삭제됨)
    public void update(String title) {
        this.title = title;
        this.sections.clear();
    }
}
