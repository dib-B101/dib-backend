package com.b101.dib.order.command.service;

import com.b101.dib.order.domain.Order;

public interface OrderCommandService {
    Order create(Long auctionId);
    Order confirm(Long memberId, Long orderId);
}
