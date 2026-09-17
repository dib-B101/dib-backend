package com.b101.dib.auction.query.dto;

// 목록 정렬. LATEST 만 id 커서, 나머지는 offset 커서 (1차)
public enum AuctionFeedSort {
    LATEST, ENDING_SOON, POPULAR, PRICE_ASC, PRICE_DESC, BID_COUNT;

    public static AuctionFeedSort from(String s) {
        if (s == null || s.isBlank()) {
            return LATEST;
        }
        try {
            return AuctionFeedSort.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LATEST;
        }
    }
}
