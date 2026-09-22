package com.b101.dib.liveBroadcast.command.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b101.dib.auction.command.service.AuctionCommandService;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.liveBroadcast.command.dto.LiveItemRequest;
import com.b101.dib.liveBroadcast.command.dto.UpdateRequest;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastRole;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.common.util.Times;
import com.b101.dib.common.validation.TradeInputValidator;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.websocket.service.LiveViewerCounter;
import com.b101.dib.websocket.service.LiveWebSocketService;
import com.b101.dib.websocket.livekit.config.LiveKitConfig;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;
import com.b101.dib.websocket.livekit.service.LiveKitService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LiveBroadcastCommandServiceImpl implements LiveBroadcastCommandService {
	
	private final LiveBroadcastRepository liveBroadcastRepository;
	private final OutboxEventRecorder outboxEventRecorder;
	private final AuctionRepository auctionRepository;
	private final AuctionCommandService auctionCommandService;
	private final ProductRepository productRepository;
	private final LiveWebSocketService liveWebSocketService;
	private final LiveViewerCounter liveViewerCounter;

	@Override
	public LiveBroadcast create(Long myId, CreateRequest request) {
		if(request.getTitle() == null) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NO_TITLE);
		}
		String roomName = "live-broadcast-" + UUID.randomUUID();
		LiveBroadcast liveBroadcast = LiveBroadcast.builder()
				.memberId(myId)
				.title(request.getTitle())
				.description(request.getDescription())
				.status(LiveBroadcastStatus.SCHEDULED)
				.livekitRoomName(roomName)
				.startedAt(request.getStartedAt())
				.endedAt(null)
				.viewCount(0)
				.createdAt(LocalDateTime.now())
				.updatedAt(null)
				.build();
		liveBroadcastRepository.save(liveBroadcast);
		return liveBroadcast;
	}

	@Override
	public LiveBroadcast update(Long myId, Long liveBroadcastId, UpdateRequest request) {
		LiveBroadcast liveBroadcast = checkLiveBroadcast(myId, liveBroadcastId);
		boolean updated = false;
		if(request.getTitle() != null) {
			liveBroadcast.setTitle(request.getTitle());
			updated = true;
		}
		if(request.getDescription() != null) {
			liveBroadcast.setDescription(request.getDescription());
			updated = true;
		}
		if(request.getStartedAt() != null) {
			liveBroadcast.setStartedAt(request.getStartedAt());
			updated = true;
		}
		if(updated) {
			liveBroadcast.setUpdatedAt(LocalDateTime.now());
		}
		return liveBroadcast;
	}

	@Override
	public LiveBroadcast delete(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = checkLiveBroadcast(myId, liveBroadcastId);
		liveBroadcastRepository.delete(liveBroadcast);
		return liveBroadcast;
	}
	
	@Transactional
	@Override
	public LiveBroadcast start(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = checkOwner(myId, liveBroadcastId);
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.LIVE) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_LIVE);
		}
		if(liveBroadcast.getStatus() != LiveBroadcastStatus.SCHEDULED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_STARTABLE);
		}
		// 한 사람이 동시에 두 방송을 켜면 송출 화면·시청자 수·채팅이 어느 방송 것인지 구분되지 않는다
		if(liveBroadcastRepository.existsByMemberIdAndStatus(myId, LiveBroadcastStatus.LIVE)) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_LIVE);
		}
		LocalDateTime now = LocalDateTime.now();
		liveBroadcast.setStatus(LiveBroadcastStatus.LIVE);
		liveBroadcast.setStartedAt(now);
		liveBroadcast.setUpdatedAt(now);

		Map<String, Object> payload = new HashMap<>();
		payload.put("liveBroadcastId", String.valueOf(liveBroadcastId));
		payload.put("title", liveBroadcast.getTitle());
		payload.put("streamUrl", liveBroadcast.getLivekitRoomName());
		payload.put("startedAt", Times.iso(now));
		liveWebSocketService.broadcast(liveBroadcastId, "LIVE_STARTED", payload);

		// 위 broadcast 는 이미 방송을 보고 있는 사람에게만 간다. 판매자 팔로우가 없으므로
		// "찜" 을 관심 신호로 삼아, 편성된 상품을 찜해 둔 사람에게 알림을 보낸다
		List<Long> productIds = auctionRepository.findAllByLiveBroadcastId(liveBroadcastId).stream()
				.map(Auction::getProductId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		if (!productIds.isEmpty()) {
			Map<String, Object> startedPayload = new HashMap<>();
			startedPayload.put("liveBroadcastId", liveBroadcastId);
			startedPayload.put("title", liveBroadcast.getTitle());
			startedPayload.put("productIds", productIds);
			outboxEventRecorder.record("LIVE_BROADCAST", liveBroadcastId, "LIVE_STARTED",
					KafkaTopics.LIVE_STARTED, startedPayload);
		}
		return liveBroadcast;
	}

	@Transactional
	@Override
	public LiveBroadcast end(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = checkOwner(myId, liveBroadcastId);
		if(liveBroadcast.getStatus() != LiveBroadcastStatus.LIVE) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_LIVE);
		}
		LocalDateTime now = LocalDateTime.now();
		liveBroadcast.setStatus(LiveBroadcastStatus.ENDED);
		liveBroadcast.setEndedAt(now);
		liveBroadcast.setUpdatedAt(now);
		// live_broadcast.view_count 는 방송 중에 갱신되지 않아 항상 0 이다. 종료 시점의 실시간 구독자 수를 확정값으로 저장한다
		int viewCount = liveViewerCounter.count(liveBroadcastId);
		liveBroadcast.setViewCount(viewCount);

		Map<String, Object> payload = new HashMap<>();
		payload.put("liveBroadcastId", String.valueOf(liveBroadcastId));
		payload.put("viewCount", viewCount);
		payload.put("endedAt", Times.iso(now));
		liveWebSocketService.broadcast(liveBroadcastId, "LIVE_ENDED", payload);
		return liveBroadcast;
	}

	@Transactional
	@Override
	public Auction startLiveAuction(Long myId, Long liveBroadcastId, Long auctionId) {
		LiveBroadcast liveBroadcast = checkOwner(myId, liveBroadcastId);
		if(liveBroadcast.getStatus() != LiveBroadcastStatus.LIVE) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_LIVE);
		}
		Auction scheduled = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
		if(!liveBroadcastId.equals(scheduled.getLiveBroadcastId())) {
			throw new BusinessException(ErrorCode.LIVE_AUCTION_NOT_MATCHED);
		}
		// 시작 규칙(소유자·상품 상태·SCHEDULED 검사, 종료시각 계산)은 일반 경매와 같아야 해서 그대로 호출한다
		Auction auction = auctionCommandService.startAuction(myId, auctionId, null, null);
		Product product = productRepository.findById(auction.getProductId()).orElse(null);

		Map<String, Object> payload = new HashMap<>();
		payload.put("liveBroadcastId", String.valueOf(liveBroadcastId));
		payload.put("auctionId", String.valueOf(auctionId));
		payload.put("startPrice", auction.getStartPrice());
		payload.put("startedAt", Times.iso(auction.getStartedAt()));
		payload.put("endedAt", Times.iso(auction.getEndedAt()));
		payload.put("auctionTime", auction.getAuctionTime());
		Map<String, Object> productPayload = new HashMap<>();
		productPayload.put("productId", String.valueOf(auction.getProductId()));
		productPayload.put("title", product == null ? null : product.getTitle());
		productPayload.put("thumbnailUrl", product == null ? null : product.getThumbnailUrl());
		payload.put("product", productPayload);
		liveWebSocketService.broadcast(liveBroadcastId, "LIVE_AUCTION_OPENED", payload);
		return auction;
	}

	// 편성 전용 테이블은 없다. 경매가 이미 들고 있는 auction.live_broadcast_id 를 채우고 비우는 것이 편성이다
	@Override
	public List<Auction> setItems(Long myId, Long liveBroadcastId, List<LiveItemRequest> items) {
		LiveBroadcast liveBroadcast = checkOwner(myId, liveBroadcastId);
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.ENDED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_ENDED);
		}
		List<LiveItemRequest> requestedItems = new ArrayList<>();
		List<Long> requested = new ArrayList<>();
		if(items != null) {
			for(LiveItemRequest item : items) {
				if(item == null || item.getAuctionId() == null) {
					throw new BusinessException(ErrorCode.INVALID_INPUT);
				}
				if(requested.contains(item.getAuctionId())) {
					continue;
				}
				requested.add(item.getAuctionId());
				requestedItems.add(item);
			}
		}

		// 목록에서 빠진 경매는 편성을 해제한다. 이미 시작된 경매는 건드리지 않는다
		for(Auction current : auctionRepository.findAllByLiveBroadcastId(liveBroadcastId)) {
			if(!requested.contains(current.getAuctionId()) && current.getStatus() != AuctionStatus.ACTIVE) {
				current.setLiveBroadcastId(null);
			}
		}

		List<Auction> result = new ArrayList<>();
		for(LiveItemRequest item : requestedItems) {
			Long auctionId = item.getAuctionId();
			Auction auction = auctionRepository.findById(auctionId)
					.orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
			Product product = productRepository.findById(auction.getProductId())
					.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
			if(!myId.equals(product.getMemberId())) {
				throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_OWNED);
			}
			if(auction.getStatus() == AuctionStatus.ENDED || auction.getStatus() == AuctionStatus.CANCELED) {
				throw new BusinessException(ErrorCode.LIVE_AUCTION_NOT_MATCHED);
			}
			// 검수 중(PENDING) 상품도 등록 시점에 경매 초안이 생겨 후보로 잡힐 수 있다. 예전엔 여기서 통과시키고
			// 방송 중 "시작" 버튼(startAuction → checkProduct)에서야 PRODUCT_PENDING 으로 터졌다.
			// 편성 저장 시점에 같은 기준으로 막아 실패를 앞당긴다. 이미 진행 중(ACTIVE)인 경매는 상품이 ON_AUCTION 이라 제외
			if(auction.getStatus() != AuctionStatus.ACTIVE) {
				checkProductRegistered(product);
			}
			// 다른 방송에 편성돼 있으면 가져오지 않는다
			if(auction.getLiveBroadcastId() != null && !liveBroadcastId.equals(auction.getLiveBroadcastId())) {
				throw new BusinessException(ErrorCode.LIVE_AUCTION_NOT_MATCHED);
			}
			auction.setLiveBroadcastId(liveBroadcastId);

			// 입찰이 진행 중인 경매의 가격·시간은 바꾸지 않는다
			if(auction.getStatus() != AuctionStatus.ACTIVE) {
				boolean updated = false;
				if(item.getStartPrice() != null) {
					if(item.getStartPrice() < 1000L) {
						throw new BusinessException(ErrorCode.AUCTION_PRICE_INVALID);
					}
					// 경매 시작과 같은 10원 단위 규칙. 편성에서 1001원을 받아두면 방송 중 입찰이 전부 INVALID_PRICE_UNIT 으로 튕긴다
					TradeInputValidator.validatePrice(item.getStartPrice());
					auction.setStartPrice(item.getStartPrice());
					// 아직 입찰이 없으므로 현재가는 시작가와 같아야 한다
					auction.setCurrentPrice(item.getStartPrice());
					updated = true;
				}
				if(item.getAuctionTime() != null) {
					// 위에서 liveBroadcastId 를 이미 채웠으므로 라이브 범위(30초~5분)로 검사된다
					auction.validateAuctionTime(item.getAuctionTime());
					auction.setAuctionTime(item.getAuctionTime());
					updated = true;
				}
				if(updated) {
					auction.setUpdatedAt(LocalDateTime.now());
				}
			}
			result.add(auction);
		}
		return result;
	}

	// AuctionCommandServiceImpl.checkProduct 와 같은 판정·에러코드. 편성 저장과 경매 시작이 다른 메시지를 내면 앱이 헷갈린다
	private void checkProductRegistered(Product product) {
		if(product.getDeletedAt() != null) {
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
		if(product.getStatus() != ProductStatus.REGISTERED) {
			throw new BusinessException(ErrorCode.PRODUCT_NOT_APPROVED);
		}
	}

	// checkLiveBroadcast 는 "아직 시작 전" 을 요구한다. 시작·종료는 조건이 반대라 소유자만 확인한다
	private LiveBroadcast checkOwner(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId)
				.orElseThrow(() -> new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND));
		if(!myId.equals(liveBroadcast.getMemberId())) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_OWNED);
		}
		return liveBroadcast;
	}

	private LiveBroadcast checkLiveBroadcast(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId)
				.orElseThrow(() -> new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND));
		if(!myId.equals(liveBroadcast.getMemberId())) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_OWNED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.LIVE) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_STARTED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.ENDED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_ENDED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.CANCELED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_CANCELED);
		}
		return liveBroadcast;
	}
}
