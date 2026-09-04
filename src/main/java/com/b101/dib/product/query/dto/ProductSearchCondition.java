package com.b101.dib.product.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCondition {
    private ProductStatus status;
    private Long categoryId;
    private Long minPrice;
    private Long maxPrice;
}