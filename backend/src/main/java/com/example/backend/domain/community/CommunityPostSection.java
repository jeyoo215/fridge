package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

// 게시글을 구성하는 반복 섹션 하나 (소제목 + 본문(리치텍스트 HTML) + 이미지/동영상 첨부)
// content는 프론트 리치텍스트 에디터가 만들어낸 HTML을 그대로 저장한다 (볼드/이탤릭/색상/폰트크기는 HTML 태그/인라인 스타일로 표현됨).
@Entity
@Table(name = "community_post_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(name = "section_order", nullable = false)
    private int sectionOrder;

    @Column(length = 100)
    private String subtitle;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 로컬에 업로드된 이미지/동영상 파일의 서빙 URL (CommunityMediaController가 업로드 시 발급)
    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", columnDefinition = "VARCHAR(10)")
    private MediaType mediaType;

    @Builder
    public CommunityPostSection(CommunityPost post, int sectionOrder, String subtitle, String content,
                                 String mediaUrl, MediaType mediaType) {
        this.post = post;
        this.sectionOrder = sectionOrder;
        this.subtitle = subtitle;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    public enum MediaType {
        IMAGE, VIDEO
    }
}
