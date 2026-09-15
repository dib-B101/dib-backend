package com.b101.dib.address.command.controller;

import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.exception.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AddressCommandController.class)
public class AddressExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAddress(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_ADDRESS;
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), detail));
    }
}
