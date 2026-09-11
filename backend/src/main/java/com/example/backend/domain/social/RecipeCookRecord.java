package com.example.backend.domain.social;

import com.example.backend.domain.recipe.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// "이 레시피 만들어봤어요" 기록. 좋아요/스크랩과 같은 방식으로 토글 처리(사용자 1명당 레시피 1개에 최대 1번).
@Entity
@Table(name = "recipe_cook_record", uniqueConstraints = @UniqueConstraint(columnNames = {"recipe_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeCookRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cook_record_id")
    private Long cookRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // TODO: 회원(인증) 기능이 만들어지면 User 엔티티에 대한 @ManyToOne으로 교체하기.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RecipeCookRecord(Recipe recipe, Long userId) {
        this.recipe = recipe;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
