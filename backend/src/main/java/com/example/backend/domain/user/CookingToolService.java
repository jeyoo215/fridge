package com.example.backend.domain.user;

import com.example.backend.domain.user.dto.CookingToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CookingToolService {

    private final CookingToolRepository cookingToolRepository;

    // 마이페이지의 조리도구 다중선택 화면에서 보여줄 전체 도구 목록 (개발자가 DB에 시드한 것)
    public List<CookingToolResponse> getAllTools() {
        return cookingToolRepository.findAll()
                .stream()
                .map(CookingToolResponse::new)
                .toList();
    }
}
