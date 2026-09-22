package com.b101.dib.auction.command.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionRequest {
    @NotNull
    @Positive
    private Long productId;
    
    @NotNull
    @Min(1000)
    private Long startPrice;
    
    // 일반 경매 생성 경로라 하한 5분. 라이브 편성 후에는 편성 API 가 30초~5분으로 덮어쓴다
    @NotNull
    @Min(300)
    private Integer auctionTime;
}
