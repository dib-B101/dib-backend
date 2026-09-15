package com.b101.dib.order.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderMapper;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.settlement.command.service.SettlementCommandService;
import java.time.LocalDateTime;

public interface OrderExpiryTxService {
    void expireOne(Long orderId);

    void confirmOne(Long orderId, LocalDateTime deliveredBefore);

    void offerNext(Long auctionId);
}
