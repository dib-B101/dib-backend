package com.b101.dib.auction.command.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuctionRequest {
    
    private Long startPrice;
    
    private Integer auctionTime;
    
    private Long liveBroadcastId;
    
}
