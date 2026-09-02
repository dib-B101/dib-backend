package com.b101.dib.product.command.dto;

import com.b101.dib.product.query.dto.ProductCondition;
import com.b101.dib.product.query.dto.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequest {
    private Long memberId;
    private Long categoryId;
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;
    private ProductStatus status;
}
