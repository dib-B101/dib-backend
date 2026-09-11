package com.b101.dib.product.repository;

import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductQueryDto;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
	List<ProductQueryDto> findAll();
	ProductDetailDto findById(@Param("productId") Long productId);
	List<AdminProductQueryDto> findForModeration(@Param("status") ProductStatus status);
}
