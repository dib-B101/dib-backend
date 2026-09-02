package com.b101.dib.product.command.dto;

import com.b101.dib.product.query.dto.ProductCondition;
import com.b101.dib.product.query.dto.ProductStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateRequest {
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private ProductStatus status;
}
