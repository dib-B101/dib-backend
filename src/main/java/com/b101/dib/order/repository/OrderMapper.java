package com.b101.dib.order.repository;

import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.dto.OrderQueryDto;
import com.b101.dib.order.query.dto.OrderShipmentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<OrderQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                       @Param("role") OrderRole role,
                                       @Param("status") OrderStatus status);
    OrderDetailDto findById(@Param("orderId") Long orderId);
    Long findWinnerId(@Param("auctionId") Long auctionId);
    Long findRunnerUpId(@Param("auctionId") Long auctionId);
    Long findMaxBidAmount(@Param("auctionId") Long auctionId, @Param("memberId") Long memberId);
    List<Long> findExpiredOfferAuctionIds(@Param("before") LocalDateTime before);
    OrderShipmentDto findShipment(@Param("orderId") Long orderId);
}
