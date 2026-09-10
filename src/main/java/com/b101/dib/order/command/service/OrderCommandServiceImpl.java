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

    @Override
    public Order create(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        if (auction.getTopBidId() == null) {
            throw new BusinessException(ErrorCode.NO_WINNING_BID);
        }
        if (orderRepository.existsByAuctionIdAndStatusNot(auctionId, OrderStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
        Product product = productRepository.findById(auction.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        Long buyerId = orderMapper.findWinnerId(auctionId);
        LocalDateTime endedAt = auction.getEndedAt() != null ? auction.getEndedAt() : LocalDateTime.now();

        Order order = Order.create(auctionId, product.getMemberId(), buyerId, auction.getCurrentPrice(), endedAt);
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
        return order;
    }
}
