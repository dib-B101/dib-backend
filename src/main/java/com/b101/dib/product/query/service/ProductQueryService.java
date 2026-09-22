package com.b101.dib.product.query.service;

import java.util.List;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductListDto;
import com.b101.dib.product.query.dto.ProductQueryDto;

public interface ProductQueryService {
	CursorPageDto<ProductQueryDto> findAll(String cursor, int size);
	ProductDetailDto findById(Long productId);
	List<AdminProductQueryDto> findForModeration(ProductStatus status);
	CursorPageDto<ProductListDto> search(com.b101.dib.product.query.dto.ProductSearchFilter filter, String cursor, int size);
	List<ProductListDto> findByMemberId(Long memberId);
	CursorPageDto<ProductListDto> findMyProducts(Long myId, String cursor, int size);
}
