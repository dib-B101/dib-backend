package com.b101.dib.productImage.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b101.dib.productImage.query.dto.ProductImageQueryDto;
import com.b101.dib.productImage.repository.ProductImageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageQueryServiceImpl implements ProductImageQueryService {
	
	private final ProductImageMapper productImageMapper;

	@Override
	public List<ProductImageQueryDto> findByProductId(Long productId) {
		return productImageMapper.findByProductId(productId);
	} 
	
	
}
