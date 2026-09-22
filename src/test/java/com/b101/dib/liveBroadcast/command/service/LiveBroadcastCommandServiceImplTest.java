package com.b101.dib.liveBroadcast.command.service;

import com.b101.dib.auction.command.service.AuctionCommandService;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.liveBroadcast.command.dto.LiveItemRequest;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.websocket.service.LiveViewerCounter;
import com.b101.dib.websocket.service.LiveWebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// 편성(setItems)은 검수 통과(REGISTERED) 상품만 받는다. 예전엔 여기서 통과시키고 방송 중 시작 버튼에서야 터졌다
@ExtendWith(MockitoExtension.class)
class LiveBroadcastCommandServiceImplTest {

    private static final long SELLER = 17L;
    private static final long LIVE = 5L;

    @Mock LiveBroadcastRepository liveBroadcastRepository;
    @Mock OutboxEventRecorder outboxEventRecorder;
    @Mock AuctionRepository auctionRepository;
    @Mock AuctionCommandService auctionCommandService;
    @Mock ProductRepository productRepository;
    @Mock LiveWebSocketService liveWebSocketService;
    @Mock LiveViewerCounter liveViewerCounter;
    @InjectMocks LiveBroadcastCommandServiceImpl service;

    @Test
    void rejectsPendingProductWhenSchedulingLiveItems() {
        givenLive();
        givenAuction(21L, 10L, AuctionStatus.SCHEDULED, null);
        givenProduct(10L, ProductStatus.PENDING);

        assertThatThrownBy(() -> service.setItems(SELLER, LIVE, List.of(item(21L))))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_PENDING));
    }

    @Test
    void rejectsRejectedProductWhenSchedulingLiveItems() {
        givenLive();
        givenAuction(21L, 10L, AuctionStatus.SCHEDULED, null);
        givenProduct(10L, ProductStatus.REJECTED);

        assertThatThrownBy(() -> service.setItems(SELLER, LIVE, List.of(item(21L))))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_APPROVED));
    }

    @Test
    void schedulesRegisteredProduct() {
        givenLive();
        Auction auction = givenAuction(21L, 10L, AuctionStatus.SCHEDULED, null);
        givenProduct(10L, ProductStatus.REGISTERED);

        List<Auction> result = service.setItems(SELLER, LIVE, List.of(item(21L)));

        assertThat(result).containsExactly(auction);
        assertThat(auction.getLiveBroadcastId()).isEqualTo(LIVE);
    }

    // 이미 진행 중인 경매는 상품이 ON_AUCTION 이다. 편성 목록을 다시 저장할 때 이걸 거절하면 방송 중 편성 수정이 불가능해진다
    @Test
    void keepsActiveAuctionAlreadyInThisLive() {
        givenLive();
        Auction active = givenAuction(21L, 10L, AuctionStatus.ACTIVE, LIVE);
        givenProduct(10L, ProductStatus.ON_AUCTION);

        List<Auction> result = service.setItems(SELLER, LIVE, List.of(item(21L)));

        assertThat(result).containsExactly(active);
    }

    private void givenLive() {
        LiveBroadcast live = LiveBroadcast.builder()
                .liveBroadcastId(LIVE)
                .memberId(SELLER)
                .status(LiveBroadcastStatus.SCHEDULED)
                .build();
        when(liveBroadcastRepository.findById(LIVE)).thenReturn(Optional.of(live));
        when(auctionRepository.findAllByLiveBroadcastId(LIVE)).thenReturn(List.of());
    }

    private Auction givenAuction(long auctionId, long productId, AuctionStatus status, Long liveBroadcastId) {
        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .productId(productId)
                .startPrice(30_000L)
                .currentPrice(30_000L)
                .auctionTime(120)
                .status(status)
                .liveBroadcastId(liveBroadcastId)
                .build();
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        return auction;
    }

    private void givenProduct(long productId, ProductStatus status) {
        Product product = Product.builder()
                .productId(productId)
                .memberId(SELLER)
                .status(status)
                .build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    }

    private static LiveItemRequest item(long auctionId) {
        return LiveItemRequest.builder().auctionId(auctionId).build();
    }
}
