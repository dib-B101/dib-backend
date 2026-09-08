package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.*;
import com.b101.dib.product.command.entity.Product;
import com.b101.dib.product.command.repository.ProductCommandRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandServiceImpl implements AuctionCommandService {

	private final AuctionRepository auctionRepository;
	private final ProductCommandRepository productRepository;

	private Auction editable(Long myId, Long auctionId) {
		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
		if (auction.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
		}
		Long productId = auction.getProductId();
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
		if (product.getMemberId() != myId) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		if (auction.getStatus() != AuctionStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
		}
		return auction;
	}

	@Override
	public Long create(Long myId, CreateAuctionRequest request) {
		Long productId = request.getProductId();
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
		Long categoryId = product.getCategoryId();
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
		return auctionRepository.save(auction).getAuctionId();
	}

	@Override
	public void update(Long myId, Long auctionId, UpdateAuctionRequest request) {
		Auction auction = editable(myId, auctionId);
		
		if (request.getStartPrice() != null) {
			auction.setStartPrice(request.getStartPrice());
			auction.setCurrentPrice(request.getStartPrice());
		}
		if (request.getAuctionTime() != null) {
			auction.setAuctionTime(request.getAuctionTime());
		}
		if (request.getLiveBroadcastId() != null) {
			auction.setLiveBroadcastId(request.getLiveBroadcastId());
		}
		auction.setUpdatedAt(LocalDateTime.now());
		// 자동으로 갱신됨
	}

	@Override
	public void delete(Long myId, Long auctionId) {
		Auction auction = editable(myId, auctionId);
		auction.setDeletedAt(LocalDateTime.now());
		auction.setStatus(AuctionStatus.CANCELED);
	}
}
