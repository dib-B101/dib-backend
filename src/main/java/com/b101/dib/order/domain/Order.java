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

    public boolean isParticipant(Long memberId) {
        return buyerId.equals(memberId) || sellerId.equals(memberId);
    }

    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.ALREADY_CONFIRMED);
        }
        if (status != OrderStatus.DELIEVERED) {
            throw new BusinessException(ErrorCode.DELIVERY_NOT_COMPLETED);
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
}
