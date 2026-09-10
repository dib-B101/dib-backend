package com.b101.dib.auth.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

public record PhoneNumber(String value) {

    public static PhoneNumber from(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }

        String normalized = rawPhoneNumber.trim().replaceAll("[\\s-]", "");
        if (!normalized.matches("010\\d{8}")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }
        return new PhoneNumber(normalized);
    }
}
