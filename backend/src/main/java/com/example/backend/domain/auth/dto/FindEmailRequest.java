package com.example.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record FindEmailRequest(
        @NotBlank String phone
) {}
