package com.b101.dib.bid.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 경매 화면 진입/재접속 시 한 번에 맞추는 스냅샷. 시각은 프론트가 Instant.parse 하므로 ISO(UTC) 문자열
@Getter
@Setter
@NoArgsConstructor
public class BidSnapshotDto {
    private Long auctionId;
    private AuctionStatus status;
    private Long startPrice;
    private Long currentPrice;
    private Long minAllowedAmount;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private String scheduledEndAt;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer extensionCount;
    @JsonProperty("isHighestBidder")
    private boolean highestBidder;
    private String serverTime;
}
