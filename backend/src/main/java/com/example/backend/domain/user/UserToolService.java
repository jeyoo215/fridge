package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.CookingToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserToolService {

    private final UserToolRepository userToolRepository;
    private final CookingToolRepository cookingToolRepository;

    // 마이페이지에서 본인이 선택한 조리도구 목록 조회
    public List<CookingToolResponse> getMyTools(Long userId) {
        return userToolRepository.findByUserId(userId)
                .stream()
                .map(CookingToolResponse::new)
                .toList();
    }

    // 다중선택 결과를 통째로 저장 (기존 선택은 지우고 새로 받은 목록으로 교체)
    @Transactional
    public void updateMyTools(Long userId, List<Long> toolIds) {
        List<CookingTool> tools = cookingToolRepository.findAllById(toolIds);
        if (tools.size() != toolIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 조리도구가 포함되어 있습니다.");
        }

        userToolRepository.deleteByUserId(userId);
        List<UserTool> userTools = tools.stream()
                .map(tool -> UserTool.builder().userId(userId).tool(tool).build())
                .toList();
        userToolRepository.saveAll(userTools);
    }
}
