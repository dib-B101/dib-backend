package com.b101.dib.product.query.mapper;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductQueryMapper {
	List<ProductQueryDto> findAll(@Param("cond") ProductSearchCondition cond);
    Optional<ProductQueryDto> findById(@Param("productId") Long productId);
}
