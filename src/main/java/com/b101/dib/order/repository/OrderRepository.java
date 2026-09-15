package com.b101.dib.order.repository;

import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByAuctionIdAndStatusNot(Long auctionId, OrderStatus status);
    List<Order> findAllByStatusAndPaymentDueBefore(OrderStatus status, LocalDateTime paymentDue);

    List<Order> findAllByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime updatedAt);
}
