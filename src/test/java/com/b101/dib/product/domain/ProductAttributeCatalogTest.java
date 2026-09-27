package com.b101.dib.product.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductAttributeCatalogTest {
    @Test
    void digitalFieldsAcceptOptionalModelAndReleaseYear() {
        assertThat(ProductAttributeCatalog.validate("디지털", Map.of("model", " Galaxy S24 ", "releaseYear", "2024")))
                .containsEntry("model", "Galaxy S24")
                .containsEntry("releaseYear", "2024");
    }

    @Test
    void rejectsFieldsFromAnotherCategory() {
        assertThatThrownBy(() -> ProductAttributeCatalog.validate("도서", Map.of("model", "S24")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
    }

    @Test
    void voucherRequiresExpiryAndUnusedConfirmation() {
        assertThatThrownBy(() -> ProductAttributeCatalog.validate("e쿠폰", null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REQUIRED_PRODUCT_ATTRIBUTES_MISSING);
        assertThatThrownBy(() -> ProductAttributeCatalog.validate("e쿠폰", Map.of("unused", "true")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REQUIRED_PRODUCT_ATTRIBUTES_MISSING);
        assertThat(ProductAttributeCatalog.validate("e쿠폰", Map.of(
                "expiryDate", LocalDate.now().plusDays(1).toString(), "unused", "true")))
                .containsEntry("unused", "true");
    }

    @Test
    void foodRejectsPastExpiryAndOpenedItem() {
        assertThatThrownBy(() -> ProductAttributeCatalog.validate("가공식품", Map.of(
                "expiryDate", LocalDate.now().minusDays(1).toString(), "unopened", "true")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ProductAttributeCatalog.validate("가공식품", Map.of(
                "expiryDate", LocalDate.now().plusDays(1).toString(), "unopened", "false")))
                .isInstanceOf(BusinessException.class);
    }
}
