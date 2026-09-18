package com.b101.dib.settlement.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.settlement.domain.Settlement;
import com.b101.dib.settlement.payout.PayoutClient;
import com.b101.dib.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettlementCommandServiceImpl implements SettlementCommandService {
    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final PayoutClient payoutClient;

    @Value("${dib.settlement.commission-rate:0.05}")
    private double commissionRate;

    @Override
    public Settlement createFor(Order order) {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.SETTLEMENT_NOT_READY);
        }
        // 보류 건은 confirm() 단계에서 이미 막히지만, 정산 행이 생기면 지급까지 흘러가므로 여기서도 이중으로 막는다
        if (order.isOnHold()) {
            throw new BusinessException(ErrorCode.ORDER_ON_HOLD);
        }
        Settlement existing = settlementRepository.findByOrderId(order.getOrderId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        Member seller = memberRepository.findById(order.getSellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Settlement settlement = settlementRepository.save(Settlement.create(order, seller, commissionRate));
        String tail = settlement.hasAccount() ? "" : " 정산 계좌가 등록되지 않았습니다. 계좌를 등록해 주세요.";
        notificationRepository.save(Notification.system(order.getSellerId(), "정산 예정",
                "주문 #" + order.getOrderId() + " 거래가 확정되어 " + settlement.getNetAmount() + "원이 정산 예정입니다." + tail));
        return settlement;
    }

    @Override
    public Settlement execute(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));
        Order order = orderRepository.findById(settlement.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 지급은 돈이 판매자에게 빠져나가는 마지막 관문이라 보류 여부를 지급 직전에 다시 본다
        if (order.isOnHold()) {
            throw new BusinessException(ErrorCode.ORDER_ON_HOLD);
        }
        if (order.getStatus() != OrderStatus.CONFIRMED || settlement.isPaidOut() || !settlement.hasAccount()) {
            throw new BusinessException(ErrorCode.SETTLEMENT_NOT_READY);
        }
        String txId;
        try {
            txId = payoutClient.payout(settlement);
        } catch (Exception e) {
            log.error("정산 지급 실패 settlementId={}", settlementId, e);
            throw new BusinessException(ErrorCode.PAYOUT_FAILED);
        }
        settlement.markPaidOut();
        notificationRepository.save(Notification.system(settlement.getSellerId(), "정산 완료",
                "주문 #" + order.getOrderId() + " 정산금 " + settlement.getNetAmount() + "원이 지급되었습니다. (" + txId + ")"));
        return settlement;
    }
}
