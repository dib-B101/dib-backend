package com.b101.dib.order.command.service;

import com.b101.dib.auction.repository.AuctionRepository;
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
    private final AuctionRepository auctionRepository;

    @Override
    public Order accept(Long memberId, Long auctionId) {
        // 같은 제안을 동시에 두 번 수락해 주문이 중복 생성되지 않도록 경매 행으로 직렬화한다.
        auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        // findRunnerUpId는 다음 제안 대상을 찾으려고 이미 제안받은 회원을 제외한다.
        // 수락 단계에서 다시 호출하면 정상 제안을 받은 회원도 항상 제외되므로, 실제 제안 알림으로 권한을 검증한다.
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
