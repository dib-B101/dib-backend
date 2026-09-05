package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.CursorPage;
import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;

import java.util.List;

public interface ProductQueryService {
	CursorPage<ProductQueryDto> findAll(ProductSearchCondition cond, String cursor);
    ProductQueryDto findById(Long productId); 
}
