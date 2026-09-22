package com.b101.dib.product.query.dto;

import com.b101.dib.product.domain.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 상품 검색 필터. 전부 선택이고 null 이면 그 조건은 안 건다.
// 조건이 늘 때마다 매퍼 시그니처를 고치지 않으려고 한 덩어리로 묶었다.
//
// 정렬은 일부러 안 넣었다 — 커서 페이징이 product_id DESC 를 전제로 하는데
// 가격순·마감임박순을 넣으면 커서 기준이 어긋나 "더 보기" 가 깨진다. 정렬은 별도 작업이다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchFilter {

    /** 제목·설명 부분 일치. 비어 있으면 조건 없이 전체 */
    private String keyword;

    private Long categoryId;

    /** 현재가 기준(입찰 전이면 시작가와 같다). 원 단위 */
    private Long minPrice;
    private Long maxPrice;

    private ProductCondition condition;

    /** true 면 지금 입찰할 수 있는 것(경매 ACTIVE)만 */
    private Boolean onAuctionOnly;

    /** 값이 뒤집혀 들어오면 조용히 바로잡는다. 400 을 주는 것보다 결과를 보여 주는 게 낫다 */
    public ProductSearchFilter normalized() {
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }
        if (minPrice != null && minPrice < 0) {
            minPrice = 0L;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            long swap = minPrice;
            minPrice = maxPrice;
            maxPrice = swap;
        }
        return this;
    }
}
