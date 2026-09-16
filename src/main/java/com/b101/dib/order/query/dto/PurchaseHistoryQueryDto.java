package com.b101.dib.order.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryQueryDto {
    /** 주문 정보 */
    private PurchaseHistoryOrderDto order;

    /** 낙찰 경매 정보 */
    private PurchaseHistoryAuctionDto auction;

    /** 구매 상품 정보 */
    private PurchaseHistoryProductDto product;

    /** 결제 정보. 결제 전이면 null */
    private PurchaseHistoryPaymentDto payment;

    public static PurchaseHistoryQueryDto from(PurchaseHistoryRowDto row) {
        PurchaseHistoryQueryDto result = new PurchaseHistoryQueryDto();

        PurchaseHistoryOrderDto order = new PurchaseHistoryOrderDto();
        order.setOrderId(row.getOrderId());
        order.setSellerId(row.getSellerId());
        order.setSellerNickname(row.getSellerNickname());
        order.setFinalPrice(row.getFinalPrice());
        order.setStatus(row.getOrderStatus());
        order.setPaymentDue(row.getPaymentDue());
        order.setCarrier(row.getCarrier());
        order.setTrackingNumber(row.getTrackingNumber());
        order.setCreatedAt(row.getOrderCreatedAt());
        result.setOrder(order);

        PurchaseHistoryAuctionDto auction = new PurchaseHistoryAuctionDto();
        auction.setAuctionId(row.getAuctionId());
        auction.setStartPrice(row.getStartPrice());
        auction.setCurrentPrice(row.getCurrentPrice());
        auction.setStatus(row.getAuctionStatus());
        auction.setEndedAt(row.getAuctionEndedAt());
        result.setAuction(auction);

        PurchaseHistoryProductDto product = new PurchaseHistoryProductDto();
        product.setProductId(row.getProductId());
        product.setTitle(row.getProductTitle());
        product.setThumbnailUrl(row.getThumbnailUrl());
        product.setCondition(row.getCondition());
        product.setModelName(row.getModelName());
        result.setProduct(product);

        if (row.getPaymentId() != null) {
            PurchaseHistoryPaymentDto payment = new PurchaseHistoryPaymentDto();
            payment.setPaymentId(row.getPaymentId());
            payment.setAmount(row.getPaymentAmount());
            payment.setType(row.getPaymentType());
            payment.setReceiptUrl(row.getReceiptUrl());
            payment.setPaidAt(row.getPaidAt());
            result.setPayment(payment);
        }

        return result;
    }
}
