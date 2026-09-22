package com.b101.dib.auction.query.dto;

import com.b101.dib.common.util.Times;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SaleHistoryQueryDto {
    /** 경매 정보 */
    private SaleHistoryAuctionDto auction;

    /** 판매 상품 정보 */
    private SaleHistoryProductDto product;

    /** 낙찰 후 생성된 주문 정보. 주문이 없으면 null */
    private SaleHistoryOrderDto order;

    public static SaleHistoryQueryDto from(SaleHistoryRowDto row) {
        SaleHistoryQueryDto result = new SaleHistoryQueryDto();

        SaleHistoryAuctionDto auction = new SaleHistoryAuctionDto();
        auction.setAuctionId(row.getAuctionId());
        auction.setStartPrice(row.getStartPrice());
        auction.setCurrentPrice(row.getCurrentPrice());
        auction.setStatus(row.getAuctionStatus());
        auction.setAuctionTime(row.getAuctionTime());
        auction.setBidCount(row.getBidCount());
        auction.setBidderCount(row.getBidderCount());
        auction.setStartedAt(Times.iso(row.getStartedAt()));
        auction.setEndedAt(Times.iso(row.getEndedAt()));
        auction.setServerTime(Times.now());
        result.setAuction(auction);

        SaleHistoryProductDto product = new SaleHistoryProductDto();
        product.setProductId(row.getProductId());
        product.setTitle(row.getProductTitle());
        product.setThumbnailUrl(row.getThumbnailUrl());
        product.setStatus(row.getProductStatus());
        result.setProduct(product);

        if (row.getOrderId() != null) {
            SaleHistoryOrderDto order = new SaleHistoryOrderDto();
            order.setOrderId(row.getOrderId());
            order.setBuyerId(row.getBuyerId());
            order.setBuyerNickname(row.getBuyerNickname());
            order.setFinalPrice(row.getFinalPrice());
            order.setStatus(row.getOrderStatus());
            order.setCreatedAt(row.getOrderCreatedAt());
            result.setOrder(order);
        }

        return result;
    }
}
