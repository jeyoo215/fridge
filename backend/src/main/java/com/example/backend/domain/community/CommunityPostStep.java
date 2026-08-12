package com.example.backend.domain.community;

import jakarta.persistence.*;
import lombok.*;

// 커뮤니티 레시피 글의 조리순서 목록 (recipe.CookingStep과 동일 구조).
// 정식 레시피로 승격될 때 이 목록이 CookingStep으로 그대로 옮겨간다.
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

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder
    public CommunityPostStep(Integer stepOrder, String description) {
        this.stepOrder = stepOrder;
        this.description = description;
    }

    void setPost(CommunityPost post) {
        this.post = post;
    }
}
