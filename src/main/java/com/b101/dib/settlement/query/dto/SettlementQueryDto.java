package com.b101.dib.settlement.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SettlementQueryDto {
    private Long settlementId;
    private Long orderId;
    private Long sellerId;
    private String productTitle;
    private Long grossAmount;
    private Long commisionFee;
    private Long netAmount;
    private LocalDateTime payoutAt;
}
