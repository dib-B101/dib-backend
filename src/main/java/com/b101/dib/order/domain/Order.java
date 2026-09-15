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
