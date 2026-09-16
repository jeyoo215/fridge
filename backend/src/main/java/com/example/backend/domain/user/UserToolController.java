package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.CookingToolResponse;
import com.example.backend.domain.user.dto.UserToolUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/tools")
@RequiredArgsConstructor
public class UserToolController {

    private final UserToolService userToolService;

    // 마이페이지: 내가 보유한 조리도구 목록
    @GetMapping
    public List<CookingToolResponse> getMyTools(@AuthenticationPrincipal Long userId) {
        return userToolService.getMyTools(userId);
    }

    // 보유 조리도구 다중선택 결과 저장 (전체 교체 방식)
    @PutMapping
    public void updateMyTools(@AuthenticationPrincipal Long userId,
                               @Valid @RequestBody UserToolUpdateRequest request) {
        userToolService.updateMyTools(userId, request.toolIds());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }
}
