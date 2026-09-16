package com.b101.dib.bid.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Redis Hot State auction:{id}:snapshot 에 저장하는 값. 회원별로 달라지는 isHighestBidder 는 topBidderId 로 읽는 쪽에서 계산
@Getter
@Setter
@NoArgsConstructor
public class BidSnapshotCacheEntry {
    private BidSnapshotDto snapshot;
    private Long topBidderId;
}
