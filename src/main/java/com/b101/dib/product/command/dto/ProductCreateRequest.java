package com.b101.dib.product.command.dto;

import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.domain.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

	private Long categoryId;
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;

    private Long startPrice;
    private Integer auctionTime;
}
