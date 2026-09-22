package com.b101.dib.bid.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BidHistoryQueryDto {
    private Long bidId;
    private Long auctionId;
    // 다른 입찰자 id 는 노출하지 않고 maskedBidderId 로 대체
    @JsonIgnore
    private Long bidderId;
    private String maskedBidderId;
    // 입찰자 닉네임. 앱은 이 값을 먼저 보여주고 없을 때만 maskedBidderId 로 대체한다 (QA: 마스킹 대신 닉네임 표시)
    private String bidderNickname;
    private Long amount;
    private LocalDateTime createdAt;
}
