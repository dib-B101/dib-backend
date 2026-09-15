package com.b101.dib.product.query.service;

import java.util.List;

import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductListDto;
import com.b101.dib.product.query.dto.ProductQueryDto;

public interface ProductQueryService {
	List<ProductQueryDto> findAll();
	ProductDetailDto findById(Long productId);
	List<AdminProductQueryDto> findForModeration(ProductStatus status);
	List<ProductListDto> search(String keyword);
	List<ProductListDto> findByMemberId(Long memberId);
	List<ProductListDto> findMyProducts(Long myId);
}
