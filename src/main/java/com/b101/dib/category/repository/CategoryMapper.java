package com.b101.dib.category.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.b101.dib.category.query.dto.CategoryQueryDto;

@Mapper
public interface CategoryMapper {

	List<CategoryQueryDto> findAll();

}
