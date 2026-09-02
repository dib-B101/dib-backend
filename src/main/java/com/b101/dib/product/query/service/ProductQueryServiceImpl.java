package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.mapper.ProductQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductQueryMapper productQueryMapper;

    @Override
    public List<ProductQueryDto> findAll() {
        List<ProductQueryDto> dtoList = productQueryMapper.findAll();
        return dtoList;
    }
}
