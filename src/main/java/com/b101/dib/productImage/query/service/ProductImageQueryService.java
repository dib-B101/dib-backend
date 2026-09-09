package com.b101.dib.productImage.query.service;

import java.util.List;

import com.b101.dib.productImage.query.dto.ProductImageQueryDto;

public interface ProductImageQueryService {

	List<ProductImageQueryDto> findByProductId(Long productId);

}
