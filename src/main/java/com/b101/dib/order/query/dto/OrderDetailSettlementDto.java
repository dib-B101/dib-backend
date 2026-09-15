package com.b101.dib.order.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailSettlementDto {
    private Long settlementId;
    private Long netAmount;
    private LocalDateTime payoutAt;
}
