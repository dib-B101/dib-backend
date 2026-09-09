package com.b101.dib.product.query.service;

import java.util.List;

import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductQueryDto;

public interface ProductQueryService {
	List<ProductQueryDto> findAll();
	ProductDetailDto findById(Long productId);
}
