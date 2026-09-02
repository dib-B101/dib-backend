package com.b101.dib.product.query.mapper;

import com.b101.dib.product.query.dto.ProductQueryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductQueryMapper {
    List<ProductQueryDto> findAll();
}
