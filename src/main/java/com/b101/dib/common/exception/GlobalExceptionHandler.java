package com.b101.dib.common.exception;

import com.b101.dib.auth.command.dto.VerificationCodeErrorResponse;
import com.b101.dib.auth.command.exception.InvalidVerificationCodeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<VerificationCodeErrorResponse> handleInvalidVerificationCode(
            InvalidVerificationCodeException e
    ) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                .body(new VerificationCodeErrorResponse(
                        errorCode.name(),
                        errorCode.getMessage(),
                        e.getRemainingAttempts()
                ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                .header("Retry-After", String.valueOf(e.getRetryAfterSeconds()))
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(
                        errorCode.name(),
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        ErrorCode errorCode = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fieldError -> "email".equals(fieldError.getField()))
                ? ErrorCode.INVALID_EMAIL
                : ErrorCode.INVALID_INPUT;

        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(
                        errorCode.name(),
                        detail
                ));
    }
}
