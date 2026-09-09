package com.b101.dib.productImage.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.b101.dib.productImage.query.dto.ProductImageQueryDto;

@Mapper
public interface ProductImageMapper {

	List<ProductImageQueryDto> findByProductId(Long productId);

}
