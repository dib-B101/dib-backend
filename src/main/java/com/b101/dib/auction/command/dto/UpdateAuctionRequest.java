package com.b101.dib.auction.command.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateAuctionRequest {
    @PositiveOrZero private Long startPrice;
    @Positive private Long bidIncrement;
    @Future private LocalDateTime startAt;
    @Future private LocalDateTime endAt;
}
