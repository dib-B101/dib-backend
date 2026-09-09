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
    @PositiveOrZero
    private Long startPrice;
    
    @NotNull
    @PositiveOrZero
    private Integer auctionTime;
}
