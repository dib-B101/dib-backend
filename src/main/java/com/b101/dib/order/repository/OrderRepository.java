package com.b101.dib.order.repository;

import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByAuctionIdAndStatusNot(Long auctionId, OrderStatus status);
    List<Order> findAllByStatusAndPaymentDueBefore(OrderStatus status, LocalDateTime paymentDue);

    List<Order> findAllByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime updatedAt);

    @Query("""
            select (count(o) > 0)
            from Order o
            where (o.sellerId = :memberId or o.buyerId = :memberId)
              and o.status in :statuses
            """)
    boolean existsActiveByMemberId(
            @Param("memberId") Long memberId,
            @Param("statuses") Set<OrderStatus> statuses
    );
}
