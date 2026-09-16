package com.b101.dib.bid.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MyBidQueryDto {
    /** 입찰 식별자 */
    private Long bidId;

    /** 입찰한 경매 식별자 */
    private Long auctionId;

    /** 입찰 금액 */
    private Long amount;

    /** 입찰 생성 시각 */
    private LocalDateTime createdAt;
}
