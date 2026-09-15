package com.b101.dib.product.query.service;

import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductListDto;
import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.repository.ProductMapper;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductMapper productQueryMapper;

    @Override
    public List<ProductQueryDto> findAll() {
        List<ProductQueryDto> dtoList = productQueryMapper.findAll();
        return dtoList;
    }

    @Override
    public ProductDetailDto findById(Long productId) {
        ProductDetailDto dto = productQueryMapper.findById(productId);
        if(dto == null) {
        	throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return dto;
    }
    
    @Override
	public List<ProductListDto> findMyProducts(Long myId) {
		return productQueryMapper.findMyProducts(myId);
	}
    
    @Override
	public List<ProductListDto> findByMemberId(Long memberId) {
		List<ProductListDto> dtoList = productQueryMapper.findByMemberId(memberId);
		return dtoList;
	}

    @Override
    public List<AdminProductQueryDto> findForModeration(ProductStatus status) {
        return productQueryMapper.findForModeration(status);
    }

	@Override
	public List<ProductListDto> search(String keyword) {
		return productQueryMapper.search(keyword);			
	}
	
}
