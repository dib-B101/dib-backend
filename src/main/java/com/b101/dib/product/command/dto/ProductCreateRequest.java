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

    @NotBlank(message = "상품명은 필수입니다.")
    private String title;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductStatus condition;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotNull(message = "시작가는 필수입니다.")
    @Positive(message = "시작가는 0보다 커야 합니다.")
    private Long startPrice;

    @NotNull(message = "경매 시간은 필수입니다.")
    @Positive(message = "경매 시간은 0보다 커야 합니다.")
    private Integer auctionTime;
}
