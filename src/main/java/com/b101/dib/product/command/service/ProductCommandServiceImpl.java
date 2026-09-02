package com.b101.dib.product.command.service;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.command.entity.Product;
import com.b101.dib.product.command.repository.ProductCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductCommandRepository productCommandRepository;

    public Long create(CreateRequest request) {
        Product product = Product.builder()
                .memberId(request.getMemberId())
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .description(request.getDescription())
                .condition(request.getCondition())
                .modelName(request.getModelName())
                .releaseYear(request.getReleaseYear())
                .marketPrice(request.getMarketPrice())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus())
                .embedding(null)
                .createdAt(LocalDateTime.now())
                .build();
        productCommandRepository.save(product);
        Long productId = product.getProductId();
        return productId;
    }

    @Override
    public void update(Long productId, UpdateRequest request) {
        Product product = productCommandRepository.findById(productId)
                .orElseThrow();
        if(request.getTitle() != null){
            product.setTitle(request.getTitle());
        }
        if(request.getDescription() != null){
            product.setDescription(request.getDescription());
        }
        if(request.getCondition() != null){
            product.setDescription(request.getDescription());
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


}
