package com.b101.dib.product.command.service;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.dto.ModerateProductRequest;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.productImage.domain.ProductImage;
import com.b101.dib.productImage.repository.ProductImageRepository;
import com.b101.dib.productImage.storage.ProductImageStorage;
import com.b101.dib.auction.command.service.AuctionStateChangedEvent;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.validation.TradeInputValidator;

import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.product.command.event.ProductModerationRequestedEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final AiServerClient aiServerClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductImageStorage productImageStorage;

    @Value("${dib.ai.moderation-enabled:false}")
    private boolean moderationEnabled;
    
    @Override
    public Product create(Long myId, ProductCreateRequest request, List<MultipartFile> images) {
		TradeInputValidator.validateReleaseYear(request.getReleaseYear());
		// 시작가는 등록 시점에 없을 수 있다. 라이브로 올리는 상품은 시작가·경매 시간을
		// 편성 단계에서 정하므로 등록 요청에 시작가가 비어 온다. update() 와 같은 규칙이다
		if (request.getStartPrice() != null) {
			TradeInputValidator.validatePrice(request.getStartPrice());
		}
		validateImages(images);
		List<String> imageUrls = productImageStorage.storeAll(images);
		String thumbnailUrl = imageUrls.get(0);

        // 추천·이상입찰 AI와 상품 검수의 준비 상태는 다르므로 검수 플래그를 따로 둔다.
        boolean requestModeration = moderationEnabled && aiServerClient.isEnabled();
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
				.status(requestModeration ? ProductStatus.PENDING : ProductStatus.REGISTERED)
        		.embedding(null)
        		.createdAt(LocalDateTime.now())
        		.updatedAt(null)
        		.deletedAt(null)
        		.build();
        productRepository.save(product);
        
        for(int i=0; i<images.size(); i++) {
        	Long productId = product.getProductId();
			Integer sequence = i+1;
            ProductImage productImage = ProductImage.builder().productId(productId)
					.imageUrl(imageUrls.get(i))
    				.sequence(sequence)
    				.build();
    		productImageRepository.save(productImage);
    	}
        
        // 검수 중에는 시작할 수 없지만, 최초 입력값은 잃지 않도록 경매 초안은 즉시 저장한다.
        auctionRepository.save(Auction.scheduled(product.getProductId(),
                request.getStartPrice(), request.getAuctionTime(), LocalDateTime.now()));

        if (requestModeration) {
            // 등록 응답이 AI 응답을 기다리면 안 되므로 커밋 후에 검수를 부른다.
            eventPublisher.publishEvent(new ProductModerationRequestedEvent(product.getProductId()));
        }

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
	    TradeInputValidator.validateReleaseYear(request.getReleaseYear());
	    if (request.getStartPrice() != null) {
	        TradeInputValidator.validatePrice(request.getStartPrice());
	    }

        Product product = checkProduct(myId, productId);
        
        boolean updated = false;
        // 검수 대상(카테고리·제목·설명)이 실제로 달라졌는지만 본다. 가격·경매시간만 바뀐 수정에 AI 를 부르지 않기 위해서다
        boolean reviewedContentChanged = false;
        if(request.getCategoryId() != null) {
            reviewedContentChanged |= !request.getCategoryId().equals(product.getCategoryId());
            product.setCategoryId(request.getCategoryId());
            updated = true;
        }
        if(request.getTitle() != null){
            reviewedContentChanged |= !request.getTitle().equals(product.getTitle());
            product.setTitle(request.getTitle());
            updated = true;
        }
        if(request.getDescription() != null){
            reviewedContentChanged |= !request.getDescription().equals(product.getDescription());
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
        
        boolean requestModeration = moderationEnabled && aiServerClient.isEnabled();
        boolean remoderate = reviewedContentChanged && requestModeration;
        if(updated) {
            if (remoderate) {
                // 내용이 달라졌으니 이전 판정은 더 이상 이 상품의 판정이 아니다
                product.setStatus(ProductStatus.PENDING);
                product.setModerationReason(null);
                product.setModerationStage(null);
                product.setModerationContentHash(null);
                product.setModeratedAt(null);
            } else if (!requestModeration && product.getStatus() != ProductStatus.PENDING) {
                // AI 미연동 기간의 기존 동작: 수정하면 즉시 재승인.
                // 검수가 켜져 있으면 검수 대상이 그대로이므로 판정도 그대로 둔다 (가격만 고쳐 거절을 무르게 둘 수 없다)
                product.setStatus(ProductStatus.REGISTERED);
            }
            product.setUpdatedAt(LocalDateTime.now());
        }

        // 신규 상품은 검수 전에도 입력값 보존용 경매 초안이 있다. 구버전 데이터만 없을 수 있다.
        Auction auction = findEditableAuction(productId);
        boolean auctionFieldRequested = request.getStartPrice() != null || request.getAuctionTime() != null;
        if (auction == null && auctionFieldRequested) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_FOUND);
        }
        if (auction != null) {
			updated = false;
			if (request.getStartPrice() != null) {
				auction.setStartPrice(request.getStartPrice());
				auction.setCurrentPrice(request.getStartPrice());
				updated = true;
			}
			if (request.getAuctionTime() != null) {
				// 여기서 안 막으면 시작할 때까지 틀린 값이 남아 있다가 startAuction 에서야 튕긴다.
				// 라이브 편성된 경매면 30초~5분, 아니면 5분 이상
				auction.validateAuctionTime(request.getAuctionTime());
				auction.setAuctionTime(request.getAuctionTime());
				updated = true;
			}
			if(updated) {
				auction.setUpdatedAt(LocalDateTime.now());
			}
        }

        if (remoderate) {
            eventPublisher.publishEvent(new ProductModerationRequestedEvent(productId));
        }

        return product;
    }
    
    @Override
    public Product delete(Long myId, Long productId) {
        
        Product product = checkProduct(myId, productId);

        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(LocalDateTime.now());
        
        List<ProductImage> productImages = productImageRepository.findAllByProductId(productId);
        productImageStorage.deleteAll(productImages.stream().map(ProductImage::getImageUrl).toList());
        for(ProductImage productImage : productImages) {
        	productImageRepository.delete(productImage);
        }
        
        // 검수 통과 전 상품에는 경매 행이 없다
        Auction auction = findEditableAuction(productId);
        if (auction != null) {
            auctionRepository.delete(auction);
        }
        
        return product;
    }
    
	@Override
	public Product startAuction(Long myId, Long productId) {
		Product product = checkProduct(myId, productId);
		if (product.getStatus() != ProductStatus.REGISTERED) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_APPROVED);
		}
		Auction auction = checkAuction(myId, productId);
		// 상품 등록 때 값을 받지 않으므로 시작 시점까지 비어 있을 수 있다
		if (auction.getStartPrice() == null || auction.getAuctionTime() == null) {
			throw new BusinessException(ErrorCode.AUCTION_PRICE_REQUIRED);
		}
		LocalDateTime now = LocalDateTime.now();
		auction.start(now);
		product.setStatus(ProductStatus.ON_AUCTION);
		product.setUpdatedAt(now);
		// 경매 커맨드 서비스의 startAuction 과 같은 이유 — 스냅샷 캐시를 새 경매 값으로 덮어쓴다
		eventPublisher.publishEvent(new AuctionStateChangedEvent(auction.getAuctionId()));
		
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
    
    // 경매 행이 없는 것은 검수 대기 상품의 정상 상태다. 있으면 수정 가능한지까지 검증한다
    private Auction findEditableAuction(Long productId) {
		Auction auction = auctionRepository.findByProductId(productId);
		if (auction == null) {
			return null;
		}
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		if (auction.getStatus() != AuctionStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
		}
		return auction;
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
        LocalDateTime now = LocalDateTime.now();
        product.setStatus(request.getStatus());
        if (request.getReason() != null && !request.getReason().isBlank()) {
            product.setModerationReason(request.getReason());
        }
        // 관리자 판단이 AI 판정을 덮었음을 남긴다
        product.setModerationStage("admin");
        product.setModeratedAt(now);
        product.setUpdatedAt(now);

        // 검수 통과 전에는 경매 행을 만들지 않았으므로 승인 시점에 만든다. 없으면 경매 시작이 불가능하다
        if (request.getStatus() == ProductStatus.REGISTERED
                && auctionRepository.findByProductId(productId) == null) {
            auctionRepository.save(Auction.scheduled(productId, null, null, now));
        }
        return product;
    }

}
