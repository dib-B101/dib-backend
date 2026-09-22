package com.b101.dib.bid.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
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
    /** 입찰한 상품 식별자 */
    private Long productId;
    /** 상품명. 내 거래 화면이 "경매 #id" 대신 보여준다 */
    private String productTitle;
    /** 상품 대표 이미지 URL */
    private String thumbnailUrl;
    /** 경매 상태. 진행 중·종료를 화면에서 가른다 */
    private AuctionStatus auctionStatus;
    /** 경매 현재가 */
    private Long currentPrice;
}
