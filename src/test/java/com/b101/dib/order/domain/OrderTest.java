package com.b101.dib.order.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OrderTest {

    @Test
    void paysPendingOrderBeforeDeadline() {
        Order order = order(OrderStatus.PENDING, LocalDateTime.now().plusHours(1));

        order.pay();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsAlreadyPaidOrder() {
        Order order = order(OrderStatus.PAID, LocalDateTime.now().plusHours(1));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(order::pay)
                .satisfies(exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_PAYMENT));
    }

    @Test
    void rejectsOrderAfterPaymentDeadline() {
        Order order = order(OrderStatus.PENDING, LocalDateTime.now().minusSeconds(1));

        assertThat(order.isPayable()).isFalse();
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(order::pay)
                .satisfies(exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_DEADLINE_EXPIRED));
    }

    private Order order(OrderStatus status, LocalDateTime paymentDue) {
        return Order.builder()
                .orderId(1L)
                .buyerId(2L)
                .sellerId(3L)
                .finalPrice(10_000L)
                .status(status)
                .paymentDue(paymentDue)
                .build();
    }
}
