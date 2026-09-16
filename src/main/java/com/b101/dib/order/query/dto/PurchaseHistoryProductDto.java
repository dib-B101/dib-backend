package com.b101.dib.order.query.dto;

import com.b101.dib.product.domain.ProductCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryProductDto {
    /** 상품 식별자 */
    private Long productId;

    /** 상품명 */
    private String title;

    /** 상품 대표 이미지 URL */
    private String thumbnailUrl;

    /** 상품 상태 등급 */
    private ProductCondition condition;

    /** 상품 모델명 */
    private String modelName;
}
