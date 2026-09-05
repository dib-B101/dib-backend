package com.b101.dib.product.query.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductSearchCondition {
    private ProductStatus status;
    private Long categoryId;
    private Long minPrice;
    private Long maxPrice;

    private Integer size = 20;   // 기본 20개
    private LocalDateTime cursorCreatedAt;
    private Long cursorProductId;

    public int getLimit() {
        return Math.min(size, 50) + 1;
    }
}