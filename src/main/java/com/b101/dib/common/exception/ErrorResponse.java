package com.b101.dib.common.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
