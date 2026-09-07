package com.b101.dib.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("code", e.getErrorCode().name());
		map.put("message", e.getErrorCode().getMessage());
		return ResponseEntity.status(e.getErrorCode().getStatus()).body(map);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
	    String detail = e.getBindingResult().getFieldErrors().stream()
	            .findFirst()
	            .map(f -> f.getField() + ": " + f.getDefaultMessage())
	            .orElse(ErrorCode.INVALID_INPUT.getMessage());
	    HashMap<String, Object> map = new HashMap<>();
	    map.put("code", ErrorCode.INVALID_INPUT.name());
	    map.put("message", detail);
	    return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus()).body(map);
	}
}
