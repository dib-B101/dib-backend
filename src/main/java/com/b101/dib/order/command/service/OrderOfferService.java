package com.b101.dib.order.command.service;

import com.b101.dib.order.domain.Order;

public interface OrderOfferService {
    Order accept(Long memberId, Long auctionId);
}
