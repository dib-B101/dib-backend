package com.b101.dib.order.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryAuctionDto {
    /** 경매 식별자 */
    private Long auctionId;

    /** 경매 시작가 */
    private Long startPrice;

    /** 최종 낙찰가 */
    private Long currentPrice;

    /** 경매 상태 */
    private AuctionStatus status;

    /** 경매 종료 시각 */
    private LocalDateTime endedAt;
}
