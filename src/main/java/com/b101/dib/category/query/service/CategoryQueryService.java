package com.b101.dib.category.query.service;

import com.b101.dib.category.query.dto.CategoryQueryDto;

import java.util.List;

public interface CategoryQueryService {
    List<CategoryQueryDto> findAll();
}
