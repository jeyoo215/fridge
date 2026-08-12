package com.example.backend.domain.challenge;

import com.example.backend.domain.challenge.dto.ChallengeResponse;
import com.example.backend.domain.challenge.dto.ChallengeStartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    // TODO: 로그인(JWT) 기능이 만들어지면 userId는 토큰에서 꺼내 쓰도록 바꾸기.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long startChallenge(@RequestParam Long userId, @Valid @RequestBody ChallengeStartRequest request) {
        return challengeService.startChallenge(userId, request);
    }

    @GetMapping("/{challengeId}")
    public ChallengeResponse getStatus(@PathVariable Long challengeId) {
        return challengeService.getStatus(challengeId);
    }

    // 예: GET /api/v1/challenges/me?userId=1
    // 진행중인 챌린지가 없으면 404 (GlobalExceptionHandler가 처리)
    @GetMapping("/me")
    public ChallengeResponse getActiveChallenge(@RequestParam Long userId) {
        return challengeService.getActiveChallenge(userId);
    }
}