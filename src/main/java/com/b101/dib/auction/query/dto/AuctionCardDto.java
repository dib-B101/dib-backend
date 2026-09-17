package com.b101.dib.auction.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.common.util.Times;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 AuctionDto (홈 추천·목록·라이브 피드 카드). 프론트 계약이 중첩(product, sellerSummary, myBid)이라 하네스 평탄화 예외
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardDto {
    private Long auctionId;
    private Long memberId;
    private Long productId;
    private Long categoryId;
    private String title;
    private String productName;
    private String categoryName;
    private Long startPrice;
    private Long currentPrice;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer bookmarkCount;
    private Integer viewCount;
    private Integer auctionTime;
    private String startedAt;
    private String scheduledEndAt;
    private String endedAt;
    private String serverTime;
    private AuctionStatus status;
    private Long liveBroadcastId;
    private boolean bookmarked;
    // 내가 낙찰받아 생성된 주문. 낙찰자 본인이 볼 때만 채워진다 (프론트 "거래 진행하기" 진입점)
    private Long myOrderId;
    private AuctionCardMyBidDto myBid;
    private AuctionCardProductDto product;
    private AuctionCardSellerDto sellerSummary;

    public static AuctionCardDto from(AuctionCardRowDto r, Long memberId) {
        AuctionCardDto d = new AuctionCardDto();
        d.setAuctionId(r.getAuctionId());
        d.setMemberId(r.getSellerId());
        d.setProductId(r.getProductId());
        d.setCategoryId(r.getCategoryId());
        d.setTitle(r.getTitle());
        d.setProductName(r.getTitle());
        d.setCategoryName(r.getCategoryName());
        d.setStartPrice(r.getStartPrice());
        d.setCurrentPrice(r.getCurrentPrice());
        d.setBidCount(r.getBidCount());
        d.setBidderCount(r.getBidderCount());
        d.setBookmarkCount(r.getBookmarkCount());
        d.setViewCount(r.getViewCount());
        d.setAuctionTime(r.getAuctionTime());
        d.setStartedAt(Times.iso(r.getStartedAt()));
        d.setScheduledEndAt(Times.iso(r.getEndedAt()));
        d.setEndedAt(r.getStatus() == AuctionStatus.ENDED ? Times.iso(r.getEndedAt()) : null);
        d.setServerTime(Times.now());
        d.setStatus(r.getStatus());
        d.setLiveBroadcastId(r.getLiveBroadcastId());
        d.setBookmarked(Boolean.TRUE.equals(r.getBookmarked()));
        d.setMyOrderId(r.getMyOrderId());
        if (memberId != null && r.getMyBidAmount() != null) {
            AuctionCardMyBidDto my = new AuctionCardMyBidDto();
            my.setAmount(r.getMyBidAmount());
            my.setHighestBidder(memberId.equals(r.getTopBidderId()));
            d.setMyBid(my);
        }
        AuctionCardProductDto p = new AuctionCardProductDto();
        p.setProductId(r.getProductId());
        p.setName(r.getTitle());
        p.setTitle(r.getTitle());
        p.setCategoryName(r.getCategoryName());
        p.setDescription(r.getDescription());
        p.setCondition(r.getCondition());
        p.setModelName(r.getModelName());
        p.setReleaseYear(r.getReleaseYear());
        p.setMarketPrice(r.getMarketPrice());
        p.setThumbnailUrl(r.getThumbnailUrl());
        d.setProduct(p);
        AuctionCardSellerDto s = new AuctionCardSellerDto();
        s.setNickname(r.getSellerNickname());
        s.setRating(r.getSellerScore());
        s.setTradeCount(r.getSellerTradeCount());
        s.setCompletedTradeCount(r.getSellerCompletedTradeCount());
        d.setSellerSummary(s);
        return d;
    }
}
