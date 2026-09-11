package com.b101.dib.product.command.service;
import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.ModerateProductRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
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

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    @Override
    public Product create(Long myId, CreateRequest request, List<MultipartFile> images) {
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
                .status(ProductStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        productRepository.save(product);
        
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
        
        // 상품 정보, 상품 이미지 AI 검수 요청을 Kafka를 통해 진행
        
        return product;
    }

    @Override
    public Product update(Long myId, Long productId, UpdateRequest request) {
    	
        Product product = checkProduct(myId, productId);
        
        boolean updated = false;
        if(request.getCategoryId() != null) {
        	product.setCategoryId(request.getCategoryId());
        	updated = true;
        }
        if(request.getTitle() != null){
            product.setTitle(request.getTitle());
            updated = true;
        }
        if(request.getDescription() != null){
            product.setDescription(request.getDescription());
            updated = true;
        }
        if(request.getCondition() != null){
            product.setCondition(request.getCondition());
            updated = true;
        }
        if(request.getModelName() != null){
            product.setModelName(request.getModelName());
            updated = true;
        }
        if(request.getReleaseYear() != null){
            product.setReleaseYear(request.getReleaseYear());
            updated = true;
        }
        if(request.getMarketPrice() != null){
            product.setMarketPrice(request.getMarketPrice());
            updated = true;
        }
        if(updated) {
        	product.setStatus(ProductStatus.PENDING);
        	product.setUpdatedAt(LocalDateTime.now());
        }
        
        return product;
    }
    
    @Override
    public Product delete(Long myId, Long productId) {
        
        Product product = checkProduct(myId, productId);

        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(LocalDateTime.now());
        
        List<ProductImage> productImages = productImageRepository.findAllByProductId(productId);
        for(ProductImage productImage : productImages) {
        	productImageRepository.delete(productImage);
        }
        return product;
    }
    
    private Product checkProduct(Long myId, Long productId) {
    	Product product = productRepository.findById(productId)
    			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    	if (!product.getMemberId().equals(myId)) {
            throw new BusinessException(ErrorCode.NOT_MY_PRODUCT);
        }
        if(product.getStatus() == ProductStatus.ON_AUCTION) {
        	throw new BusinessException(ErrorCode.PRODUCT_ON_AUCTION);
        }
        if(product.getStatus() == ProductStatus.SOLD) {
        	throw new BusinessException(ErrorCode.PRODUCT_ALREADY_SOLD);
        }
        if (product.getDeletedAt() != null) {
        	throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        
        return product;
    }


    @Override
    public Product moderate(Long productId, ModerateProductRequest request) {
        if (request.getStatus() != ProductStatus.REGISTERED && request.getStatus() != ProductStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        if (product.getStatus() != ProductStatus.PENDING) {
            throw new BusinessException(ErrorCode.PRODUCT_MODERATION_NOT_ALLOWED);
        }
        product.setStatus(request.getStatus());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
