package com.b101.dib.common.validation;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

public final class TradeInputValidator {
    private static final int MIN_RELEASE_YEAR = 1900;
    private static final long PRICE_UNIT = 10L;

    private TradeInputValidator() {
    }

    public static void validateReleaseYear(Integer releaseYear) {
        if (releaseYear != null && releaseYear < MIN_RELEASE_YEAR) {
            throw new BusinessException(ErrorCode.INVALID_RELEASE_YEAR);
        }
    }

    public static void validatePrice(Long price) {
        if (price == null || price <= 0L || price % PRICE_UNIT != 0L) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_UNIT);
        }
    }
}
