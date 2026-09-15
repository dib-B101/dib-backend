package com.b101.dib.payment.toss;

import java.util.List;

public record TossPaymentResponse(String paymentKey, String orderId, String status, String approvedAt,
                                  Long totalAmount, Receipt receipt, List<Cancel> cancels) {
    public record Receipt(String url) {
    }

    public record Cancel(String transactionKey, Long cancelAmount, String cancelReason) {
    }

    public String lastTransactionKey() {
        return cancels == null || cancels.isEmpty() ? null : cancels.get(cancels.size() - 1).transactionKey();
    }
}
