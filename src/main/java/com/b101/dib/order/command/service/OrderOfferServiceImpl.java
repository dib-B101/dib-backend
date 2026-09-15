package com.b101.dib.order.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.domain.NotificationType;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderOfferServiceImpl implements OrderOfferService {
    static final int OFFER_HOURS = 24;

    private final OrderMapper orderMapper;
    private final NotificationRepository notificationRepository;
    private final OrderCommandService orderCommandService;

    @Override
    public Order accept(Long memberId, Long auctionId) {
        Long runnerUp = orderMapper.findRunnerUpId(auctionId);
        if (runnerUp == null || !runnerUp.equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Notification offer = notificationRepository
                .findTopByAuctionIdAndMemberIdAndTypeAndTitleOrderByCreatedAtDesc(
                        auctionId, memberId, NotificationType.AUCTION_WON, Notification.OFFER_TITLE)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        if (offer.getCreatedAt().plusHours(OFFER_HOURS).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OFFER_EXPIRED);
        }
        Long amount = orderMapper.findMaxBidAmount(auctionId, memberId);
        return orderCommandService.create(auctionId, memberId, amount);
    }
}
