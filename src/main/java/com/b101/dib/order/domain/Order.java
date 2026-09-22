package com.b101.dib.order.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"order\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private static final int PAYMENT_DUE_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Long auctionId;
    private Long sellerId;
    private Long buyerId;
    private Long finalPrice;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderStatus status;

    private LocalDateTime paymentDue;

    @JdbcTypeCode(SqlTypes.JSON)
    private String address;

    private String carrier;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String chattingSessionId;

    // 신고 보류. status를 건드리지 않으므로 해제하면 원래 흐름이 그대로 이어진다
    private LocalDateTime heldAt;
    private Long holdReportId;

    public static Order create(Long auctionId, Long sellerId, Long buyerId, Long finalPrice, LocalDateTime endedAt) {
        return Order.builder()
                .auctionId(auctionId)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .finalPrice(finalPrice)
                .status(OrderStatus.PENDING)
                .paymentDue(endedAt.plusHours(PAYMENT_DUE_HOURS))
                .chattingSessionId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isBuyer(Long memberId) {
        return buyerId.equals(memberId);
    }

    public boolean isSeller(Long memberId) {
        return sellerId.equals(memberId);
    }

    public boolean isParticipant(Long memberId) {
        return buyerId.equals(memberId) || sellerId.equals(memberId);
    }

    public boolean isPayable() {
        return status == OrderStatus.PENDING && !paymentDue.isBefore(LocalDateTime.now());
    }

    public boolean isPaidOrLater() {
        return status == OrderStatus.PAID || status == OrderStatus.SHIPPED
                || status == OrderStatus.DELIEVERED || status == OrderStatus.CONFIRMED;
    }

    public boolean isClosed() {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELED || status == OrderStatus.REFUNDED;
    }

    public boolean isOnHold() {
        return heldAt != null;
    }

    public void hold(Long reportId) {
        // 이미 끝난 거래는 막을 것도, 되돌릴 것도 없다 (정산/환불 경로가 이미 확정됐거나 닫혔다)
        if (isClosed() || isOnHold()) {
            return;
        }
        this.heldAt = LocalDateTime.now();
        this.holdReportId = reportId;
        touch();
    }

    public void releaseHold() {
        if (!isOnHold()) {
            return;
        }
        this.heldAt = null;
        this.holdReportId = null;
        touch();
    }

    public void pay() {
        if (status == OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
        }
        if (status != OrderStatus.PENDING || paymentDue.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAYMENT_DEADLINE_EXPIRED);
        }
        this.status = OrderStatus.PAID;
        touch();
    }

    public void expire() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_DEADLINE_EXPIRED);
        }
        this.status = OrderStatus.CANCELED;
        touch();
    }

    public void refund() {
        if (status != OrderStatus.PAID && status != OrderStatus.SHIPPED && status != OrderStatus.DELIEVERED) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED);
        }
        this.status = OrderStatus.REFUNDED;
        touch();
    }

    public void updateAddress(String addressJson) {
        if (status != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_EDITABLE);
        }
        this.address = addressJson;
        touch();
    }

    public void ship(String carrier, String trackingNumber) {
        // 조사 중에 물건이 실제로 발송되면 되돌릴 수 없어 환불·반환 처리가 꼬인다. 배송지 수정은 거래를 진행시키지 않으므로 막지 않는다
        if (isOnHold()) {
            throw new BusinessException(ErrorCode.ORDER_ON_HOLD);
        }
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIEVERED) {
            throw new BusinessException(ErrorCode.SHIPMENT_ALREADY_EXISTS);
        }
        if (status != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUIRED);
        }
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_REQUIRED);
        }
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
        touch();
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            return;
        }
        this.status = OrderStatus.DELIEVERED;
        touch();
    }

    public void confirm() {
        // 확정되면 환불 경로(PAID/SHIPPED/DELIEVERED만 허용)가 닫히고 정산이 생긴다 — 신고 조사 중에는 확정 불가
        if (isOnHold()) {
            throw new BusinessException(ErrorCode.ORDER_ON_HOLD);
        }
        if (status == OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.ALREADY_CONFIRMED);
        }
        if (status != OrderStatus.DELIEVERED) {
            throw new BusinessException(ErrorCode.DELIVERY_NOT_COMPLETED);
        }
        this.status = OrderStatus.CONFIRMED;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
