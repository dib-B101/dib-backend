package com.b101.dib.product.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductAttributeCatalogTest {
    @Test
    void everyActiveCategoryHasRegistrationFields() {
        // 로컬 및 배포 DB에서 유지 중인 카테고리명. 이름이 달라지면 입력 항목도 함께 갱신해야 한다.
        List<String> categoryNames = List.of(
                "디지털기기", "생활가전", "가구·인테리어", "스포츠·레저",
                "패션·잡화", "뷰티", "취미·게임", "예술·창작", "생활/주방",
                "유아동", "유아도서", "도서", "티켓/교환권", "e쿠폰",
                "가공식품", "건강기능식품", "반려동물용품", "식물", "기타");

        assertThat(categoryNames).allSatisfy(name ->
                assertThat(ProductAttributeCatalog.forCategory(name))
                        .as("%s category fields", name).isNotEmpty());
        assertThat(ProductAttributeCatalog.forCategory("디지털기기"))
                .extracting(ProductAttributeSpec::label).contains("모델명", "출시연도");
        assertThat(ProductAttributeCatalog.forCategory("예술·창작"))
                .extracting(ProductAttributeSpec::label).contains("작가·제작자", "재료·기법");
    }

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
