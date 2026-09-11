package com.b101.dib.payment.toss;

public record TossPaymentResponse(String paymentKey, String orderId, String status, String approvedAt,
                                  Long totalAmount, Receipt receipt) {
    public record Receipt(String url) {
    }
}
