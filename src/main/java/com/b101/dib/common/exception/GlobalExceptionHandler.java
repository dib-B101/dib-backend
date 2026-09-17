package com.b101.dib.common.exception;

import com.b101.dib.auth.command.dto.VerificationCodeErrorResponse;
import com.b101.dib.auth.exception.InvalidVerificationCodeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage()));
    }

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

    // X-Member-Id 는 인증 필터가 주입한다. 비어 있다는 건 인증이 안 됐다는 뜻이므로 400 이 아니라 401 로 내려야
    // 앱이 "로그인이 풀렸다" 로 알아듣고 재로그인시킨다
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {

        ErrorCode errorCode = "X-Member-Id".equalsIgnoreCase(e.getHeaderName())
                ? ErrorCode.UNAUTHORIZED
                : ErrorCode.INVALID_INPUT;

        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        boolean hasEmailError = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fieldError -> "email".equals(fieldError.getField()));
        boolean hasPasswordPolicyError = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fieldError -> "ValidPassword".equals(fieldError.getCode()));

        ErrorCode errorCode;
        if (hasEmailError) {
            errorCode = ErrorCode.INVALID_EMAIL;
        } else if (hasPasswordPolicyError) {
            errorCode = ErrorCode.INVALID_PASSWORD;
        } else {
            errorCode = ErrorCode.INVALID_INPUT;
        }

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
