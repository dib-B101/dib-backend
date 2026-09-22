package com.b101.dib.product.query.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchKeywordStoreTest {

    @Test
    void trimsAndCollapsesWhitespace() {
        assertThat(SearchKeywordStore.normalize("  빈티지   카메라 ")).isEqualTo("빈티지 카메라");
    }

    // 빈 검색어와 30자를 넘는 검색어는 인기 검색어 후보가 아니다
    @Test
    void rejectsBlankAndTooLongKeyword() {
        assertThat(SearchKeywordStore.normalize("   ")).isNull();
        assertThat(SearchKeywordStore.normalize(null)).isNull();
        assertThat(SearchKeywordStore.normalize("가".repeat(31))).isNull();
        assertThat(SearchKeywordStore.normalize("가".repeat(30))).hasSize(30);
    }
}
