package com.example.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;

// ERD의 cooking_tool 테이블 (조리도구 마스터). 종류는 개발자가 DB에 직접 시드하고,
// 사용자는 마이페이지에서 보유한 도구를 이 목록 중에서 골라 선택한다.
@Entity
@Table(name = "cooking_tool")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CookingTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tool_id")
    private Long toolId;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Builder
    public CookingTool(String toolName) {
        this.toolName = toolName;
    }
}
