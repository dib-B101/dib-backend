package com.b101.dib.category.repository;

import com.b101.dib.category.query.dto.CategoryQueryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<CategoryQueryDto> findAll();
}
