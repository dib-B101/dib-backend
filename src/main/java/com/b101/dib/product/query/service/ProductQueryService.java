package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.ProductQueryDto;

import java.util.List;

public interface ProductQueryService {
    List<ProductQueryDto> findAll();
}
