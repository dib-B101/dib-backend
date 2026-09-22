package com.b101.dib.order.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderMapper;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.review.command.service.ReviewRequestNotifier;
import com.b101.dib.settlement.command.service.SettlementCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandServiceImpl implements OrderCommandService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final SettlementCommandService settlementCommandService;
    private final ReviewRequestNotifier reviewRequestNotifier;

    @Override
    public Order create(Long auctionId) {
        Auction auction = findAuction(auctionId);
        if (auction.getTopBidId() == null) {
            throw new BusinessException(ErrorCode.NO_WINNING_BID);
        }
        Long buyerId = orderMapper.findWinnerId(auctionId);
        LocalDateTime endedAt = auction.getEndedAt() != null ? auction.getEndedAt() : LocalDateTime.now();
        return createInternal(auction, buyerId, auction.getCurrentPrice(), endedAt);
    }

    @Override
    public Order create(Long auctionId, Long buyerId, Long finalPrice) {
        Auction auction = findAuction(auctionId);
        return createInternal(auction, buyerId, finalPrice, LocalDateTime.now());
    }

    private Auction findAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private Order createInternal(Auction auction, Long buyerId, Long finalPrice, LocalDateTime endedAt) {
        if (orderRepository.existsByAuctionIdAndStatusNot(auction.getAuctionId(), OrderStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
        Product product = productRepository.findById(auction.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        Order order = Order.create(auction.getAuctionId(), product.getMemberId(), buyerId, finalPrice, endedAt);
        return orderRepository.save(order);
    }

    @Override
    public Order confirm(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isBuyer(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        order.confirm();
        settlementCommandService.createFor(order);
        reviewRequestNotifier.requestFor(order);   // 거래가 끝난 지금이 별점을 물어볼 유일한 타이밍이다
        return order;
    }
}
