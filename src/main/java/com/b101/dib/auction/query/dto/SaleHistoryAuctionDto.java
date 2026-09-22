package com.b101.dib.auction.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /** 경매 진행 시간(초) */
    private Integer auctionTime;

    /** 누적 입찰 횟수 */
    private Integer bidCount;

    /** 경매 참여자 수 */
    private Integer bidderCount;

    /** 경매 시작 시각. 프론트가 Instant.parse 하므로 ISO UTC 문자열로 내보낸다 */
    private String startedAt;

    /** 경매 종료(예정) 시각. 목록·상세와 같은 ISO UTC 문자열 */
    private String endedAt;

    /** 남은 시간 계산 기준 시각 */
    private String serverTime;
}
