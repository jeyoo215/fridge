package com.example.backend.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 닉네임은 여기서 받지 않음 — 가입 후 마이페이지에서 직접 설정함 (AuthService.signup이 임시 닉네임을 채워줌).
public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,

        // 아이디 찾기 본인 확인용. 010-1234-5678 또는 01012345678 형식만 허용.
        @NotBlank @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
        String phone
) {}
