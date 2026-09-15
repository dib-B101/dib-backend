package com.b101.dib.payment.toss;

public record TossWebhookEvent(String eventType, String createdAt, Data data) {
    public record Data(String paymentKey, String orderId, String status) {
    }
}
