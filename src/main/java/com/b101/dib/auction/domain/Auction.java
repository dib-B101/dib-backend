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
        if (topBidId == null) {
            return startPrice;
        }
        return currentPrice + increment(currentPrice);
    }

    public boolean isActiveAt(LocalDateTime now) {
        return status == AuctionStatus.ACTIVE && endedAt != null && now.isBefore(endedAt);
    }

    public void start(LocalDateTime now) {
        if (status != AuctionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.AUCTION_STARTED);
        }
        if (auctionTime == null || auctionTime < 300) {
            throw new BusinessException(ErrorCode.AUCTION_SCHEDULE_INVALID);
        }
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

    public void end(LocalDateTime now) {
        status = AuctionStatus.ENDED;
        if (endedAt == null || endedAt.isAfter(now)) {
            endedAt = now;
        }
        updatedAt = now;
    }
}
