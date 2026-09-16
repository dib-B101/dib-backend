package com.b101.dib.category.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b101.dib.category.query.dto.CategoryQueryDto;
import com.b101.dib.category.repository.CategoryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

	private final CategoryMapper categoryMapper;
	@Override
	public List<CategoryQueryDto> findAll() {
		return categoryMapper.findAll();
	}

}
