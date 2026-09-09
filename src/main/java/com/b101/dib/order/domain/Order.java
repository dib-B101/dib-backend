package com.b101.dib.order.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
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
