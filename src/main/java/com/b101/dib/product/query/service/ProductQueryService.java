package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;

import java.util.List;

public interface ProductQueryService {
	List<ProductQueryDto> findAll(ProductSearchCondition cond);
    ProductQueryDto findById(Long productId); 
}
