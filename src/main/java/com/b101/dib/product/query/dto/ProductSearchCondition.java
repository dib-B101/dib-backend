package com.b101.dib.product.query.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter                          // ?status=ACTIVE 같은 쿼리스트링을 객체에 넣을 때 setter 필요
public class ProductSearchCondition {
    // 필터 (전부 선택)
    private ProductStatus status;
    private Long categoryId;
    private Long minPrice;
    private Long maxPrice;

    // 페이징
    private Integer size = 20;   // 기본 20개

    // 커서를 풀어서 넣는 자리 (클라이언트가 직접 채우지 않음 — 서비스가 채운다)
    private LocalDateTime cursorCreatedAt;
    private Long cursorProductId;

    public int getLimit() {      // 최대 50개로 제한, hasNext 판단용으로 1개 더 가져온다
        return Math.min(size, 50) + 1;
    }
}
