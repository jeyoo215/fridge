package com.example.backend.domain.challenge;

import com.example.backend.domain.challenge.dto.ChallengeHistoryPageResponse;
import com.example.backend.domain.challenge.dto.ChallengeResponse;
import com.example.backend.domain.challenge.dto.ChallengeStartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long startChallenge(@AuthenticationPrincipal Long userId, @Valid @RequestBody ChallengeStartRequest request) {
        return challengeService.startChallenge(userId, request);
    }

    @GetMapping("/{challengeId}")
    public ChallengeResponse getStatus(@PathVariable("challengeId") Long challengeId) {
        return challengeService.getStatus(challengeId);
    }

    // 진행중인 챌린지가 없으면 404 (GlobalExceptionHandler가 처리)
    @GetMapping("/me")
    public ChallengeResponse getActiveChallenge(@AuthenticationPrincipal Long userId) {
        return challengeService.getActiveChallenge(userId);
    }

    @GetMapping("/me/history")
    public ChallengeHistoryPageResponse getHistory(@AuthenticationPrincipal Long userId,
                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                    @RequestParam(name = "size", defaultValue = "5") int size) {
        return challengeService.getHistory(userId, page, size);
    }

    @PatchMapping("/{challengeId}/abort")
    public ChallengeResponse abortChallenge(@PathVariable("challengeId") Long challengeId) {
        return challengeService.abortChallenge(challengeId);
    }
}
