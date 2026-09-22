package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.CreateAuctionRequest;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionCommandServiceImplTest {

    @Mock AuctionRepository auctionRepository;
    @Mock ProductRepository productRepository;
    @Mock OutboxEventRecorder outboxEventRecorder;
    @InjectMocks AuctionCommandServiceImpl auctionCommandService;

    @Test
    void rejectsSecondAuctionForProduct() {
        Product product = approvedProduct();
        Auction existing = scheduledAuction();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(auctionRepository.findByProductId(10L)).thenReturn(existing);

        CreateAuctionRequest request = CreateAuctionRequest.builder()
                .productId(10L)
                .startPrice(30_000L)
                .auctionTime(300)
                .build();

        assertThatThrownBy(() -> auctionCommandService.create(17L, request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUCTION_ALREADY_EXISTS));
    }

    @Test
    void startsExistingScheduledAuctionAndMovesProductToOnAuction() {
        Product product = approvedProduct();
        Auction auction = scheduledAuction();
        when(auctionRepository.findById(21L)).thenReturn(Optional.of(auction));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        Auction started = auctionCommandService.startAuction(17L, 21L, null, null);

        assertThat(started.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(started.getStartedAt()).isNotNull();
        assertThat(started.getEndedAt()).isAfter(started.getStartedAt());
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_AUCTION);
        // 찜한 사람에게 "시작했다" 를 알리는 경로. 이 이벤트가 빠지면 찜 알림이 통째로 안 간다
        verify(outboxEventRecorder).record(eq("AUCTION"), eq(21L), eq("AUCTION_STARTED"),
                eq(KafkaTopics.AUCTION_STARTED), any());
    }

    // 라이브 편성 상품은 방송 시작 때 LIVE_STARTED 로 이미 알렸다. 물건마다 또 보내면 도배가 된다
    @Test
    void doesNotAnnounceStartForLiveScheduledAuction() {
        Product product = approvedProduct();
        Auction auction = scheduledAuction();
        auction.setLiveBroadcastId(7L);
        auction.setAuctionTime(30);   // 라이브는 30초까지 허용된다
        when(auctionRepository.findById(21L)).thenReturn(Optional.of(auction));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        auctionCommandService.startAuction(17L, 21L, null, null);

        verify(outboxEventRecorder, never()).record(any(), any(), any(), any(), any());
    }

    private Product approvedProduct() {
        return Product.builder()
                .productId(10L)
                .memberId(17L)
                .status(ProductStatus.REGISTERED)
                .build();
    }

    private Auction scheduledAuction() {
        return Auction.builder()
                .auctionId(21L)
                .productId(10L)
                .startPrice(30_000L)
                .currentPrice(30_000L)
                .auctionTime(300)
                .status(AuctionStatus.SCHEDULED)
                .build();
    }
}
