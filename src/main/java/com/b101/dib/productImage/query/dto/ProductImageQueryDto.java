package com.b101.dib.productImage.query.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductImageQueryDto {
	private Long productImageId;
	private Long productId;
	private String imageUrl;
	private Integer sequence;
}
