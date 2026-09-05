package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.CursorPage;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;

public interface ProductQueryService {
	CursorPage<ProductQueryDto> findAll(ProductSearchCondition cond, String cursor);
	ProductDetailDto findById(Long productId);
}
