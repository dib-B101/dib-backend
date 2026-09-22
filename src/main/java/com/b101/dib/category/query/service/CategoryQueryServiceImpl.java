package com.b101.dib.category.query.service;

import com.b101.dib.category.query.dto.CategoryQueryDto;
import com.b101.dib.category.repository.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryQueryDto> findAll() {
        return categoryMapper.findAll();
    }
}
