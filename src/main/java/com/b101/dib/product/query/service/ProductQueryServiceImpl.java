package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;
import com.b101.dib.product.query.mapper.ProductQueryMapper;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductQueryMapper productQueryMapper;

    @Override
    public List<ProductQueryDto> findAll(ProductSearchCondition cond) {
        return productQueryMapper.findAll(cond);
    }

    @Override
    public ProductQueryDto findById(Long productId) {
        return productQueryMapper.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
