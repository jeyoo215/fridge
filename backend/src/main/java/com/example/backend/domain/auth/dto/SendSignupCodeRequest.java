package com.example.backend.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendSignupCodeRequest(
        @NotBlank @Email String email
) {}
