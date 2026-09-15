package com.b101.dib.order.query.dto;

import com.b101.dib.product.domain.ProductCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailProductDto {
    private Long productId;
    private String title;
    private String thumbnailUrl;
    private ProductCondition condition;
    private String modelName;
}
