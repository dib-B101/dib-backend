package com.b101.dib.auction.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "auction")
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;
    private Long productId;
    private Long startPrice;
    private Long currentPrice;
    private Long topBidId;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AuctionStatus status;
    
    private Integer bidCount;
    private Integer bidderCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Integer extensionCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    private Long liveBroadcastId;

    // 입찰 규칙 — 마감 15초 안에 입찰이 들어오면 남은 시간을 15초로 되돌린다 (연장이 아니라 리셋)
    public static final int EXTEND_WINDOW_SECONDS = 15;

    // 경매 시간 허용 범위.
    // 일반 경매는 며칠씩 열어 두는 물건이라 하한만 둔다.
    // 라이브는 방송 중에 한 점씩 파는 자리라 5분짜리를 걸면 한 시간에 12점이 한계다.
    // 30초까지 내리고, 대신 방송이 한 물건에 묶이지 않게 5분 상한을 둔다.
    public static final int MIN_AUCTION_SECONDS = 300;
    public static final int LIVE_MIN_AUCTION_SECONDS = 30;
    public static final int LIVE_MAX_AUCTION_SECONDS = 300;

    // 검수 통과 직후와 AI 미연동 등록에서 같은 초기값으로 경매 행을 만들려고 한곳에 모았다.
    // startPrice·auctionTime 은 null 이면 "아직 정하지 않음" 이고 경매 시작 시점에 정한다
    public static Auction scheduled(Long productId, Long startPrice, Integer auctionTime, LocalDateTime now) {
        return Auction.builder()
                .productId(productId)
                .startPrice(startPrice)
                .currentPrice(startPrice)
                .topBidId(null)
                .auctionTime(auctionTime)
                .startedAt(null)
                .endedAt(null)
                .status(AuctionStatus.SCHEDULED)
                .bidCount(0)
                .bidderCount(0)
                .viewCount(0)
                .bookmarkCount(0)
                .extensionCount(0)
                .createdAt(now)
                .updatedAt(null)
                .deletedAt(null)
                .liveBroadcastId(null)
                .build();
    }

    // 현재가 구간별 최소 입찰 단위
    public static long increment(long price) {
        if (price < 10_000L) {
            return 500L;
        }
        if (price < 100_000L) {
            return 1_000L;
        }
        if (price < 1_000_000L) {
            return 5_000L;
        }
        return 10_000L;
    }

    // 다음 입찰이 최소 얼마여야 하는지. 첫 입찰은 시작가 그대로 허용
    public long minNextBid() {
        // 시작가가 아직 정해지지 않은 경매도 조회될 수 있어 언박싱 NPE 를 막는다
        if (startPrice == null) {
            return 0L;
        }
        if (topBidId == null) {
            return startPrice;
        }
        return currentPrice + increment(currentPrice);
    }

    public boolean isActiveAt(LocalDateTime now) {
        return status == AuctionStatus.ACTIVE && endedAt != null && now.isBefore(endedAt);
    }

    // 라이브 편성 여부는 live_broadcast_id 하나로 판단한다 (편성 전용 테이블이 없다)
    public boolean isLiveItem() {
        return liveBroadcastId != null;
    }

    // 경매 시간 검사. 허용 범위가 라이브냐 아니냐로 갈리므로 값을 쓰는 쪽마다 숫자를 박지 않고 여기서 본다.
    // 편성(setItems)·수정(products PATCH)·시작(startAuction) 세 경로가 전부 이걸 거친다
    public void validateAuctionTime(Integer seconds) {
        if (seconds == null) {
            throw new BusinessException(ErrorCode.AUCTION_SCHEDULE_INVALID);
        }
        if (isLiveItem()) {
            if (seconds < LIVE_MIN_AUCTION_SECONDS || seconds > LIVE_MAX_AUCTION_SECONDS) {
                throw new BusinessException(ErrorCode.AUCTION_SCHEDULE_INVALID);
            }
            return;
        }
        if (seconds < MIN_AUCTION_SECONDS) {
            throw new BusinessException(ErrorCode.AUCTION_SCHEDULE_INVALID);
        }
    }

    public void start(LocalDateTime now) {
        if (status != AuctionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.AUCTION_STARTED);
        }
        validateAuctionTime(auctionTime);
        status = AuctionStatus.ACTIVE;
        startedAt = now;
        endedAt = now.plusSeconds(auctionTime);
        updatedAt = now;
    }

    // 입찰 반영. 락 잡은 상태에서 호출. 마감 리셋이 일어났으면 true
    public boolean applyBid(Long bidId, long amount, boolean firstBidOfMember, LocalDateTime now) {
        currentPrice = amount;
        topBidId = bidId;
        bidCount = bidCount + 1;
        if (firstBidOfMember) {
            bidderCount = bidderCount + 1;
        }
        updatedAt = now;
        if (Duration.between(now, endedAt).getSeconds() < EXTEND_WINDOW_SECONDS) {
            endedAt = now.plusSeconds(EXTEND_WINDOW_SECONDS);
            extensionCount = extensionCount + 1;
            return true;
        }
        return false;
    }

    // 유찰(입찰 0건) 경매는 행을 새로 만들지 않고 이 행을 초기화해 재사용한다.
    // findByProductId 가 단건 조회라 같은 상품에 경매 행이 둘이 되면 조회 자체가 깨진다
    public void relist(LocalDateTime now) {
        if (status != AuctionStatus.ENDED) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_RELISTABLE);
        }
        if (topBidId != null) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_RELISTABLE);
        }
        status = AuctionStatus.SCHEDULED;
        startPrice = null;
        currentPrice = null;
        auctionTime = null;
        startedAt = null;
        endedAt = null;
        topBidId = null;
        bidCount = 0;
        bidderCount = 0;
        extensionCount = 0;
        // 종료된 Live에 묶인 채로 두면 GENERAL/LIVE 편성 후보 조회에서 영구히 빠진다.
        // 재등록은 이전 편성을 끝내고 새 경매 용도로 되돌리는 동작이다.
        liveBroadcastId = null;
        updatedAt = now;
    }

    public void end(LocalDateTime now) {
        status = AuctionStatus.ENDED;
        if (endedAt == null || endedAt.isAfter(now)) {
            endedAt = now;
        }
        updatedAt = now;
    }
}
