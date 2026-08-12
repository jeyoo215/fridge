package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.CookingToolResponse;
import com.example.backend.domain.user.dto.UserToolUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/tools")
@RequiredArgsConstructor
public class UserToolController {

    private final UserToolService userToolService;

    // 마이페이지: 내가 보유한 조리도구 목록
    // 예: GET /api/v1/users/me/tools?userId=1
    @GetMapping
    public List<CookingToolResponse> getMyTools(@RequestParam("userId") Long userId) {
        return userToolService.getMyTools(userId);
    }

    // 보유 조리도구 다중선택 결과 저장 (전체 교체 방식)
    // 예: PUT /api/v1/users/me/tools?userId=1
    @PutMapping
    public void updateMyTools(@RequestParam("userId") Long userId,
                               @Valid @RequestBody UserToolUpdateRequest request) {
        userToolService.updateMyTools(userId, request.toolIds());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
