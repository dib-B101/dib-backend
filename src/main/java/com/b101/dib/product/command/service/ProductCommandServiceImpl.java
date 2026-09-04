package com.b101.dib.product.command.service;
import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.command.entity.Product;
import com.b101.dib.product.command.repository.ProductCommandRepository;
import com.b101.dib.product.query.dto.ProductStatus;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductCommandRepository productCommandRepository;
    
    @Override
    public Long create(Long memberId, CreateRequest request) {
        Product product = Product.builder()
                .memberId(memberId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .description(request.getDescription())
                .condition(request.getCondition())
                .modelName(request.getModelName())
                .releaseYear(request.getReleaseYear())
                .marketPrice(request.getMarketPrice())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(ProductStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
        productCommandRepository.save(product);
        return product.getProductId();
    }

    @Override
    public void update(Long productId, UpdateRequest request) {
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if(request.getTitle() != null){
            product.setTitle(request.getTitle());
        }
        if(request.getDescription() != null){
            product.setDescription(request.getDescription());
        }
        if(request.getCondition() != null){
            product.setCondition(request.getCondition());
        }
        if(request.getModelName() != null){
            product.setModelName(request.getModelName());
        }
        if(request.getReleaseYear() != null){
            product.setReleaseYear(request.getReleaseYear());
        }
        if(request.getMarketPrice() != null){
            product.setMarketPrice(request.getMarketPrice());
        }
        if(request.getStatus() != null){
            product.setStatus(request.getStatus());
        }
    }
    
    @Override
    public void delete(Long memberId, Long productId) {
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        if (!product.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_DELETABLE);
        }

        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.HIDDEN);
    }

}
