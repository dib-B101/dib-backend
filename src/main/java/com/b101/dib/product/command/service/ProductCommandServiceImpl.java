package com.b101.dib.product.command.service;
import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.command.repository.ProductCommandRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.productImage.domain.ProductImage;
import com.b101.dib.productImage.repository.ProductImageRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductCommandRepository productCommandRepository;
    private final ProductImageRepository productImageRepository;
    
    @Override
    public Long create(Long myId, CreateRequest request, List<MultipartFile> images) {
    	if(images != null && images.size() > 10) {
    		throw new BusinessException(ErrorCode.TOO_MUCH_IMAGES);
    	}
    	String thumbnailUrl = "thumbnail-url";
        Product product = Product.builder()
                .memberId(myId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .description(request.getDescription())
                .condition(request.getCondition())
                .modelName(request.getModelName())
                .releaseYear(request.getReleaseYear())
                .marketPrice(request.getMarketPrice())
                .thumbnailUrl(thumbnailUrl)
                .status(ProductStatus.REGISTERED)
                .createdAt(LocalDateTime.now())
                .build();
        productCommandRepository.save(product);
        
        // 이미지들을 업로드한다.
        // S3 서비스로 구현할 예정
        for(int i=0; i<images.size(); i++) {
        	MultipartFile image = images.get(i);
        	Long productId = product.getProductId();
    		String imageUrl = image.toString();
    		Integer sequence = i+1;
    		ProductImage productImage = ProductImage.builder().productId(productId)
    				.imageUrl(imageUrl)
    				.sequence(sequence)
    				.build();
    		productImageRepository.save(productImage);
    	}
        return product.getProductId();
    }

    @Override
    public void update(Long memberId, Long productId, UpdateRequest request) {
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        if (!product.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
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
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_DELETABLE);
        }

        product.setDeletedAt(LocalDateTime.now());
    }

}
