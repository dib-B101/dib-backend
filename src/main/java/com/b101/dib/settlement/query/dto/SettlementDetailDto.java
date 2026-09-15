package com.b101.dib.settlement.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SettlementDetailDto {
    private Long settlementId;
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
    private String productTitle;
    private OrderStatus orderStatus;
    private Long grossAmount;
    private Long commisionFee;
    private Long netAmount;
    private String bankName;
    private String maskedAccountNumber;
    private LocalDateTime payoutAt;
}
