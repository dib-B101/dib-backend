package com.b101.dib.category.query.service;

import java.util.List;

import com.b101.dib.category.query.dto.CategoryQueryDto;

public interface CategoryQueryService {

	List<CategoryQueryDto> findAll();

}
