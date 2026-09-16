package com.b101.dib.auction.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SaleHistoryAuctionDto {
    /** 경매 식별자 */
    private Long auctionId;

    /** 경매 시작가 */
    private Long startPrice;

    /** 현재가 또는 최종 낙찰가 */
    private Long currentPrice;

    /** 경매 상태 */
    private AuctionStatus status;

    /** 누적 입찰 횟수 */
    private Integer bidCount;

    /** 경매 참여자 수 */
    private Integer bidderCount;

    /** 경매 시작 시각 */
    private LocalDateTime startedAt;

    /** 경매 종료 시각 */
    private LocalDateTime endedAt;
}
