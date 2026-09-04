package com.b101.dib.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    PRODUCT_NOT_DELETABLE(HttpStatus.CONFLICT, "DRAFT 상태의 상품만 삭제할 수 있습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"),
	PRODUCT_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 상품입니다");
    private final HttpStatus status;
    private final String message;
}