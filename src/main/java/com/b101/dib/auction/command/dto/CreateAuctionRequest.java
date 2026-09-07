package com.b101.dib.auction.command.dto;

import com.b101.dib.auction.query.dto.AuctionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CreateAuctionRequest {
    @NotNull @Positive private Long productId;
    @NotNull @PositiveOrZero private Long startPrice;
    @NotNull @Positive private Long bidIncrement;
    @NotNull @Future private LocalDateTime startAt;
    @NotNull @Future private LocalDateTime endAt;
    @NotNull private AuctionType auctionType;
}
