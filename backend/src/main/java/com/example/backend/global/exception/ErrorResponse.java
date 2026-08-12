package com.example.backend.global.exception;

import java.util.List;

public record ErrorResponse(
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String reason) {}

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public static ErrorResponse of(String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(message, fieldErrors);
    }
}