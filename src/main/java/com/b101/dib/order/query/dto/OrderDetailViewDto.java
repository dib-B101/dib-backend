package com.b101.dib.order.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 주문 상세(50) 응답. 명세가 중첩 구조라 하네스 평탄화 예외(2번) 적용 — Mapper 행(OrderDetailDto)을 조립한다
@Getter
@Setter
@NoArgsConstructor
public class OrderDetailViewDto {
    private OrderDetailOrderDto order;
    private OrderDetailAuctionDto auction;
    private OrderDetailProductDto product;
    // payment 는 결제 전, settlement 는 확정 전이면 null
    private OrderDetailPaymentDto payment;
    private OrderDetailSettlementDto settlement;
    private boolean chattingReadOnly;

    public static OrderDetailViewDto from(OrderDetailDto row) {
        OrderDetailViewDto view = new OrderDetailViewDto();

        OrderDetailOrderDto order = new OrderDetailOrderDto();
        order.setOrderId(row.getOrderId());
        order.setAuctionId(row.getAuctionId());
        order.setProductId(row.getProductId());
        order.setSellerId(row.getSellerId());
        order.setSellerNickname(row.getSellerNickname());
        order.setBuyerId(row.getBuyerId());
        order.setBuyerNickname(row.getBuyerNickname());
        order.setFinalPrice(row.getFinalPrice());
        order.setStatus(row.getStatus());
        order.setPaymentDue(row.getPaymentDue());
        order.setAddress(row.getAddress());
        order.setCarrier(row.getCarrier());
        order.setTrackingNumber(row.getTrackingNumber());
        order.setChattingSessionId(row.getChattingSessionId());
        order.setCreatedAt(row.getCreatedAt());
        order.setUpdatedAt(row.getUpdatedAt());
        order.setHeldAt(row.getHeldAt());
        order.setHoldReportId(row.getHoldReportId());
        view.setOrder(order);

        OrderDetailAuctionDto auction = new OrderDetailAuctionDto();
        auction.setAuctionId(row.getAuctionId());
        auction.setProductId(row.getProductId());
        auction.setTitle(row.getProductTitle());
        auction.setStartPrice(row.getStartPrice());
        auction.setCurrentPrice(row.getCurrentPrice());
        auction.setStatus(row.getAuctionStatus());
        auction.setEndedAt(row.getAuctionEndedAt());
        view.setAuction(auction);

        OrderDetailProductDto product = new OrderDetailProductDto();
        product.setProductId(row.getProductId());
        product.setTitle(row.getProductTitle());
        product.setThumbnailUrl(row.getThumbnailUrl());
        product.setCondition(row.getCondition());
        product.setModelName(row.getModelName());
        view.setProduct(product);

        if (row.getPaymentId() != null) {
            OrderDetailPaymentDto payment = new OrderDetailPaymentDto();
            payment.setPaymentId(row.getPaymentId());
            payment.setOrderId(row.getOrderId());
            payment.setAmount(row.getPaymentAmount());
            payment.setType(row.getPaymentType());
            payment.setPaidAt(row.getPaidAt());
            view.setPayment(payment);
        }

        if (row.getSettlementId() != null) {
            OrderDetailSettlementDto settlement = new OrderDetailSettlementDto();
            settlement.setSettlementId(row.getSettlementId());
            settlement.setNetAmount(row.getNetAmount());
            settlement.setPayoutAt(row.getPayoutAt());
            view.setSettlement(settlement);
        }

        OrderStatus s = row.getStatus();
        view.setChattingReadOnly(s == OrderStatus.CONFIRMED || s == OrderStatus.CANCELED || s == OrderStatus.REFUNDED);
        return view;
    }
}
