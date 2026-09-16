package com.b101.dib.auction.query.dto;

import com.b101.dib.product.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SaleHistoryProductDto {
    /** 상품 식별자 */
    private Long productId;

    /** 상품명 */
    private String title;

    /** 상품 대표 이미지 URL */
    private String thumbnailUrl;

    /** 상품 상태 */
    private ProductStatus status;
}
