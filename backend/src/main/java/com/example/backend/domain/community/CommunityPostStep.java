package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

// 커뮤니티 레시피 글의 조리순서 목록 (번호가 매겨진 단계 하나 = 예전의 "섹션"과 같은 역할을 겸함).
// description은 프론트 리치텍스트 에디터가 만든 HTML(볼드/이탤릭/색상/폰트크기는 인라인 스타일로 표현)이고,
// 이미지/동영상도 단계별로 첨부할 수 있다. 정식 레시피로 승격될 때 이 목록이 CookingStep으로 옮겨간다.
@Entity
@Table(name = "community_post_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    // 로컬에 업로드된 이미지/동영상 파일의 서빙 URL (CommunityMediaController가 업로드 시 발급)
    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", columnDefinition = "VARCHAR(10)")
    private MediaType mediaType;

    @Builder
    public CommunityPostStep(Integer stepOrder, String description, String mediaUrl, MediaType mediaType) {
        this.stepOrder = stepOrder;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    void setPost(CommunityPost post) {
        this.post = post;
    }

    public enum MediaType {
        IMAGE, VIDEO
    }
}
