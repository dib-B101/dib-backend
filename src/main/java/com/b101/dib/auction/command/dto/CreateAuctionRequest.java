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
    
    @NotNull
    @Min(300)
    private Integer auctionTime;
}
