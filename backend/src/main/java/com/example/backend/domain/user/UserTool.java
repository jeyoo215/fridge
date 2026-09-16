package com.example.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;

// ERD의 user_tool 테이블 (유저가 보유한 조리도구, cooking_tool과의 다대다를 잇는 매핑 테이블)
@Entity
@Table(name = "user_tool")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_tool_id")
    private Long userToolId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private CookingTool tool;

    @Builder
    public UserTool(Long userId, CookingTool tool) {
        this.userId = userId;
        this.tool = tool;
    }
}
