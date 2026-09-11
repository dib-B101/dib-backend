package com.b101.dib.payment.toss;

public record TossBillingKeyResponse(String billingKey, String customerKey, String cardCompany, Card card) {
    public record Card(String number, String cardType, String ownerType) {
    }
}
