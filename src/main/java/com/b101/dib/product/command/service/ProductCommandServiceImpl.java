package com.b101.dib.product.command.service;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.dto.ModerateProductRequest;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.productImage.domain.ProductImage;
import com.b101.dib.productImage.repository.ProductImageRepository;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandServiceImpl implements ProductCommandService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final AuctionRepository auctionRepository;
    
    @Override
    public Product create(Long myId, ProductCreateRequest request, List<MultipartFile> images) {
		validateImages(images);
    	String thumbnailUrl = images.get(0).toString();
    	
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
				// AI 검수 미연동 기간: 등록 즉시 승인 처리한다.
				.status(ProductStatus.REGISTERED)
        		.embedding(null)
        		.createdAt(LocalDateTime.now())
        		.updatedAt(null)
        		.deletedAt(null)
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
        
        // 실제 AI 검수 연결 시 REGISTERED 전이를 비동기 검수 결과로 교체한다.
        
        Auction auction = Auction.builder()
        		.productId(product.getProductId())
        		.startPrice(request.getStartPrice())
        		.currentPrice(request.getStartPrice())
        		.topBidId(null)
        		.auctionTime(request.getAuctionTime())
        		.startedAt(null)
        		.endedAt(null)
				.status(AuctionStatus.SCHEDULED)
        		.bidCount(0).bidderCount(0).viewCount(0).bookmarkCount(0).extensionCount(0)
        		.createdAt(LocalDateTime.now())
        		.updatedAt(null)
        		.deletedAt(null)
        		.build();
        auctionRepository.save(auction);
        
        return product;
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_REQUIRED);
        }
        if (images.size() > 10) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
            }
            if (image.getSize() > MAX_IMAGE_BYTES) {
                throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
            }
            String declaredType = normalizeContentType(image.getContentType());
            String detectedType = detectContentType(image);
            if (declaredType == null || !declaredType.equals(detectedType)) {
                throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
            }
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) return null;
        String normalized = contentType.toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(normalized)) return "image/jpeg";
        return switch (normalized) {
            case "image/jpeg", "image/png", "image/webp" -> normalized;
            default -> null;
        };
    }

    private String detectContentType(MultipartFile image) {
        try (InputStream input = image.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (header.length >= 3
                    && (header[0] & 0xff) == 0xff
                    && (header[1] & 0xff) == 0xd8
                    && (header[2] & 0xff) == 0xff) {
                return "image/jpeg";
            }
            byte[] pngSignature = new byte[] {
                    (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
            };
            if (header.length >= 8 && Arrays.equals(Arrays.copyOf(header, 8), pngSignature)) {
                return "image/png";
            }
            if (header.length >= 12
                    && matchesAscii(header, 0, "RIFF")
                    && matchesAscii(header, 8, "WEBP")) {
                return "image/webp";
            }
            return null;
        } catch (IOException exception) {
            return null;
        }
    }

    private boolean matchesAscii(byte[] bytes, int offset, String expected) {
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != (byte) expected.charAt(index)) return false;
        }
        return true;
    }

    @Override
    public Product update(Long myId, Long productId, ProductUpdateRequest request) {
    	
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
			// AI 검수 미연동 기간: 수정 상품도 즉시 재승인 처리한다.
			product.setStatus(ProductStatus.REGISTERED);
        	product.setUpdatedAt(LocalDateTime.now());
        }
        
        Auction auction = checkAuction(myId, productId);
		
		updated = false;
		if (request.getStartPrice() != null) {
			auction.setStartPrice(request.getStartPrice());
			auction.setCurrentPrice(request.getStartPrice());
			updated = true;
		}
		if (request.getAuctionTime() != null) {
			auction.setAuctionTime(request.getAuctionTime());
			updated = true;
		}
		if(updated) {
			auction.setUpdatedAt(LocalDateTime.now());			
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
        
        Auction auction = checkAuction(myId, productId);
        auctionRepository.delete(auction);
        
        return product;
    }
    
	@Override
	public Product startAuction(Long myId, Long productId) {
		Product product = checkProduct(myId, productId);
		if (product.getStatus() != ProductStatus.REGISTERED) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_APPROVED);
		}
		Auction auction = checkAuction(myId, productId);
		LocalDateTime now = LocalDateTime.now();
		auction.start(now);
		product.setStatus(ProductStatus.ON_AUCTION);
		product.setUpdatedAt(now);
		
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
    
    private Auction checkAuction(Long myId, Long productId) {
		Auction auction = auctionRepository.findByProductId(productId);
		if(auction == null) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_FOUND);
		}
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		if (auction.getStatus() != AuctionStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
		}
		return auction;
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
