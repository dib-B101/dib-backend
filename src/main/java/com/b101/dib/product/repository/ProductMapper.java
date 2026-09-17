package com.b101.dib.product.repository;

import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductListDto;
import com.b101.dib.product.query.dto.ProductQueryDto;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
	List<ProductQueryDto> findAll(@Param("cursor") Long cursor, @Param("limit") int limit);
	ProductDetailDto findById(@Param("productId") Long productId);
	List<AdminProductQueryDto> findForModeration(@Param("status") ProductStatus status);
	List<ProductListDto> findMyProducts(@Param("myId") Long myId,
										@Param("cursor") Long cursor,
										@Param("limit") int limit);
	List<ProductListDto> findByMemberId(@Param("memberId") Long memberId);
	List<ProductListDto> search(@Param("keyword") String keyword,
								@Param("cursor") Long cursor,
								@Param("limit") int limit);
}
