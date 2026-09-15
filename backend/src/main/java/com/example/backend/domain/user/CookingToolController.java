package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.CookingToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cooking-tools")
@RequiredArgsConstructor
public class CookingToolController {

    private final CookingToolService cookingToolService;

    // 마이페이지 조리도구 다중선택 화면에 보여줄 전체 도구 목록 (유저별 개인화 아님, 공용 마스터 데이터)
    // 예: GET /api/v1/cooking-tools
    @GetMapping
    public List<CookingToolResponse> getAllTools() {
        return cookingToolService.getAllTools();
    }
}
