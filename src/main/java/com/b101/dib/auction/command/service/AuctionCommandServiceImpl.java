package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.common.exception.*;
import com.b101.dib.common.validation.TradeInputValidator;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandServiceImpl implements AuctionCommandService {

	private final AuctionRepository auctionRepository;
	private final ProductRepository productRepository;
	private final OutboxEventRecorder outboxEventRecorder;

	@Override
	public Auction create(Long myId, CreateAuctionRequest request) {
		TradeInputValidator.validatePrice(request.getStartPrice());
		Long productId = request.getProductId();
		Product product = checkProduct(myId, productId);
		if (auctionRepository.findByProductId(productId) != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_EXISTS);
		}
		
		Auction auction = Auction.builder()
				.productId(productId)
				.startPrice(request.getStartPrice())
				.currentPrice(request.getStartPrice())
				.auctionTime(request.getAuctionTime())
				.startedAt(null)
				.endedAt(null)
				.status(AuctionStatus.SCHEDULED)
                .bidCount(0)
                .bidderCount(0)
                .viewCount(0)
                .bookmarkCount(0)
                .topBidId(null)
                .extensionCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .deletedAt(null)
                .liveBroadcastId(null)
				.build();
		auctionRepository.save(auction).getAuctionId();
		return auction;
	}

	@Override
	public Auction update(Long myId, Long auctionId, UpdateAuctionRequest request) {
		if (request.getStartPrice() != null) {
			TradeInputValidator.validatePrice(request.getStartPrice());
		}
		Auction auction = checkAuction(myId, auctionId);
		
		boolean updated = false;
		if (request.getStartPrice() != null) {
			auction.setStartPrice(request.getStartPrice());
			auction.setCurrentPrice(request.getStartPrice());
			updated = true;
		}
		if (request.getAuctionTime() != null) {
			auction.setAuctionTime(request.getAuctionTime());
			updated = true;
		}
		if (request.getLiveBroadcastId() != null) {
			auction.setLiveBroadcastId(request.getLiveBroadcastId());
			updated = true;
		}
		if(updated) {
			auction.setUpdatedAt(LocalDateTime.now());			
		}
		// 자동으로 갱신됨
		
		return auction;
	}
	
	
	@Override
	public Auction startAuction(Long myId, Long auctionId, Long startPrice, Integer auctionTime) {
		Auction auction = checkAuction(myId, auctionId);
		Long productId = auction.getProductId();
		Product product = checkProduct(myId, productId);
		LocalDateTime now = LocalDateTime.now();

		if (startPrice != null) {
			if (startPrice < 1000L) {
				throw new BusinessException(ErrorCode.AUCTION_PRICE_INVALID);
			}
			auction.setStartPrice(startPrice);
			// 아직 입찰이 없으므로 현재가는 시작가와 같아야 한다
			auction.setCurrentPrice(startPrice);
		}
		if (auctionTime != null) {
			auction.validateAuctionTime(auctionTime);   // 라이브 편성이면 30초~5분, 아니면 5분 이상
			auction.setAuctionTime(auctionTime);
		}
		// 상품 등록 때 값을 받지 않으므로 시작 시점까지 비어 있을 수 있다
		if (auction.getStartPrice() == null || auction.getAuctionTime() == null) {
			throw new BusinessException(ErrorCode.AUCTION_PRICE_REQUIRED);
		}

		auction.start(now);
		product.setStatus(ProductStatus.ON_AUCTION);
		product.setUpdatedAt(now);

		// 찜한 사람들에게 "시작했다" 를 알리기 위한 이벤트. 팬아웃이 수백 건이 될 수 있어 트랜잭션 밖(Consumer)에서 처리한다.
		// 라이브 편성 상품은 제외한다 — 방송 시작 때 LIVE_STARTED 로 이미 알렸는데 물건마다 또 보내면 도배가 된다
		if (!auction.isLiveItem()) {
			Map<String, Object> payload = new HashMap<>();
			payload.put("auctionId", auction.getAuctionId());
			payload.put("productId", productId);
			payload.put("productTitle", product.getTitle());
			payload.put("sellerId", product.getMemberId());
			outboxEventRecorder.record("AUCTION", auction.getAuctionId(), "AUCTION_STARTED",
					KafkaTopics.AUCTION_STARTED, payload);
		}

		return auction;
	}

	// 유찰된 경매를 다시 예정 상태로. 상품도 REGISTERED 로 되돌려 시작가·시간을 다시 정할 수 있게 한다
	@Override
	public Auction relist(Long myId, Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		Product product = checkProduct(myId, auction.getProductId());
		if (product.getStatus() == ProductStatus.SOLD) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_RELISTABLE);
		}
		auction.relist(LocalDateTime.now());
		product.setStatus(ProductStatus.REGISTERED);
		return auction;
	}

	@Override
	public Auction delete(Long myId, Long auctionId) {
		Auction auction = checkAuction(myId, auctionId);
		auction.setDeletedAt(LocalDateTime.now());
		auction.setStatus(AuctionStatus.CANCELED);
		
		return auction;
	}
	
	private Product checkProduct(Long myId, Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
		if (!product.getMemberId().equals(myId)) {
            throw new BusinessException(ErrorCode.NOT_MY_PRODUCT);
        }
		if (product.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
		}
		if(product.getStatus() == ProductStatus.PENDING) {
			throw new BusinessException(ErrorCode.PRODUCT_PENDING);
		}
        if(product.getStatus() == ProductStatus.ON_AUCTION) {
        	throw new BusinessException(ErrorCode.PRODUCT_ON_AUCTION);
        }
        if(product.getStatus() == ProductStatus.SOLD) {
        	throw new BusinessException(ErrorCode.PRODUCT_ALREADY_SOLD);
        }
		if (product.getStatus() != ProductStatus.REGISTERED) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_APPROVED);
		}
        return product;
	}
	
	private Auction checkAuction(Long myId, Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		if (auction.getStatus() != AuctionStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
		}
		Product product = productRepository.findById(auction.getProductId())
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
		if (!product.getMemberId().equals(myId)) {
			throw new BusinessException(ErrorCode.NOT_MY_PRODUCT);
		}
		return auction;
	}

}
