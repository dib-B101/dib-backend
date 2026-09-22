package com.b101.dib.common.validation;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeInputValidatorTest {

    @Test
    void allowsMissingOr1900ReleaseYear() {
        assertThatCode(() -> TradeInputValidator.validateReleaseYear(null)).doesNotThrowAnyException();
        assertThatCode(() -> TradeInputValidator.validateReleaseYear(1900)).doesNotThrowAnyException();
    }

    @Test
    void rejectsReleaseYearBefore1900() {
        assertThatThrownBy(() -> TradeInputValidator.validateReleaseYear(1899))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_RELEASE_YEAR));
    }

    @Test
    void allowsPositiveTenWonPrice() {
        assertThatCode(() -> TradeInputValidator.validatePrice(10L)).doesNotThrowAnyException();
        assertThatCode(() -> TradeInputValidator.validatePrice(12_340L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullZeroNegativeAndNonTenWonPrice() {
        for (Long price : new Long[]{null, 0L, -10L, 11L}) {
            assertThatThrownBy(() -> TradeInputValidator.validatePrice(price))
                    .isInstanceOfSatisfying(BusinessException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_UNIT));
        }
    }
}
