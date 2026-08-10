package com.example.backend.global.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// 프로젝트 전체 공통 예외 처리.
// 컨트롤러에 개별 @ExceptionHandler가 이미 있으면 그게 우선 적용되고,
// 여기 없는 컨트롤러들은 이 핸들러로 떨어짐.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 존재하지 않는 리소스 조회 (레시피, 챌린지 등 findById 실패)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException e) {
        return ErrorResponse.of(e.getMessage());
    }

    // 잘못된 요청 값 (본인 소유 아님, 유효하지 않은 파라미터 등)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        return ErrorResponse.of(e.getMessage());
    }

    // 현재 상태에서 허용되지 않는 요청 (예: 이미 진행중인 챌린지가 있는데 또 시작)
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException e) {
        return ErrorResponse.of(e.getMessage());
    }

    // @Valid 검증 실패 (요청 body의 필드 값이 조건에 안 맞을 때)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ErrorResponse.of("입력값이 올바르지 않습니다.", fieldErrors);
    }

    // 그 외 예상 못한 서버 예외 (로그는 남기되, 클라이언트엔 상세 스택트레이스 노출 안 함)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("예상하지 못한 서버 예외 발생", e);
        return ErrorResponse.of("서버 오류가 발생했습니다.");
    }
}