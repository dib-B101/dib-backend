package com.b101.dib.payment.domain;

import com.b101.dib.order.domain.Order;
import com.b101.dib.payment.toss.TossPaymentResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final Order order = Order.builder().orderId(7L).buyerId(2L).finalPrice(58_000L).build();

    @Test
    void recordsTossApprovalInstantAsKoreanLocalTime() {
        TossPaymentResponse response = response("2026-09-15T15:30:00Z");

        assertThat(Payment.approved(order, response).getPaidAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 16, 0, 30));
    }

    @Test
    void preservesKoreanApprovalTimeWithoutAddingNineHoursAgain() {
        TossPaymentResponse response = response("2026-09-16T00:30:00+09:00");

        assertThat(Payment.approved(order, response).getPaidAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 16, 0, 30));
    }

    @Test
    void usesKoreanTimeWhenProviderOmitsApprovalTime() {
        LocalDateTime before = LocalDateTime.now(SEOUL);

        LocalDateTime paidAt = Payment.approved(order, response(null)).getPaidAt();

        assertThat(paidAt).isBetween(before, LocalDateTime.now(SEOUL));
    }

    private TossPaymentResponse response(String approvedAt) {
        return new TossPaymentResponse("payment-key", "toss-order", "DONE", approvedAt,
                58_000L, null, null);
    }
}
