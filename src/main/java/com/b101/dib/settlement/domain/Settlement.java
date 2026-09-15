package com.b101.dib.settlement.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.order.domain.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    private Long orderId;
    private Long sellerId;
    private Long grossAmount;
    private Long commisionFee;
    private Long netAmount;
    private String bankName;
    private String accountNumber;
    private LocalDateTime payoutAt;

    public static Settlement create(Order order, Member seller, double commissionRate) {
        long gross = order.getFinalPrice();
        long fee = (long) Math.floor(gross * commissionRate);
        return Settlement.builder()
                .orderId(order.getOrderId())
                .sellerId(order.getSellerId())
                .grossAmount(gross)
                .commisionFee(fee)
                .netAmount(gross - fee)
                .bankName(seller.getBankName())
                .accountNumber(seller.getAccountNumber())
                .build();
    }

    public boolean isPaidOut() {
        return payoutAt != null;
    }

    public boolean hasAccount() {
        return bankName != null && accountNumber != null;
    }

    public void markPaidOut() {
        if (isPaidOut() || !hasAccount()) {
            throw new BusinessException(ErrorCode.SETTLEMENT_NOT_READY);
        }
        this.payoutAt = LocalDateTime.now();
    }
}
