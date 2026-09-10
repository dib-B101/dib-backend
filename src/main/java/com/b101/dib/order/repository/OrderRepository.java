package com.b101.dib.order.repository;

import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByAuctionIdAndStatusNot(Long auctionId, OrderStatus status);
}
