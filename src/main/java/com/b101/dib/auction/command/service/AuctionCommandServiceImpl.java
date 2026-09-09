package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.*;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductCommandRepository;
import com.b101.dib.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandServiceImpl implements AuctionCommandService {

	private final AuctionRepository auctionRepository;
	private final ProductRepository productRepository;

	@Override
	public Auction create(Long myId, CreateAuctionRequest request) {
		Long productId = request.getProductId();
		Product product = checkProduct(myId, productId);
		
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
	public Auction startAuction(Long myId, Long auctionId) {
		Auction auction = checkAuction(myId, auctionId);
		auction.setStatus(AuctionStatus.ACTIVE);
		auction.setStartedAt(LocalDateTime.now());
		auction.setUpdatedAt(LocalDateTime.now());
		
		Long productId = auction.getProductId();
		Product product = checkProduct(myId, productId);
		product.setStatus(ProductStatus.ON_AUCTION);
		product.setUpdatedAt(LocalDateTime.now());
		
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
		if(product.getStatus() == ProductStatus.PENDING) {
			throw new BusinessException(ErrorCode.PRODUCT_PENDING);
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
	
	private Auction checkAuction(Long myId, Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		if (auction.getStatus() != AuctionStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
		}
		return auction;
	}

}
