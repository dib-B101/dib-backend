package com.b101.dib.devtools;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.command.service.OrderExpiryTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("local")
@RequiredArgsConstructor
@Transactional
public class DevScenarioService {
    private final JdbcTemplate jdbcTemplate;
    private final AuctionRepository auctionRepository;
    private final OrderExpiryTxService orderExpiryTxService;

    public Map<String, Object> bid(Long auctionId, Long memberId, Long amount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
        }
        Long current = auction.getCurrentPrice() == null ? 0L : auction.getCurrentPrice();
        boolean firstBid = auction.getTopBidId() == null;
        if (amount < current || (!firstBid && amount.equals(current))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Long bidId;
        try {
            bidId = jdbcTemplate.queryForObject(
                    "INSERT INTO bid (auction_id, member_id, amount) VALUES (?, ?, ?) RETURNING bid_id",
                    Long.class, auctionId, memberId, amount);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);   // 같은 금액이 먼저 들어감 → 1명만 성공
        }
        Integer bidders = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT member_id) FROM bid WHERE auction_id = ?", Integer.class, auctionId);
        auction.setCurrentPrice(amount);
        auction.setTopBidId(bidId);
        auction.setBidCount((auction.getBidCount() == null ? 0 : auction.getBidCount()) + 1);
        auction.setBidderCount(bidders);
        auction.setUpdatedAt(LocalDateTime.now());

        Map<String, Object> map = new HashMap<>();
        map.put("bidId", bidId);
        map.put("auctionId", auctionId);
        map.put("memberId", memberId);
        map.put("amount", amount);
        map.put("currentPrice", auction.getCurrentPrice());
        map.put("bidCount", auction.getBidCount());
        return map;
    }

    public Auction end(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        auction.setStatus(AuctionStatus.ENDED);
        auction.setEndedAt(LocalDateTime.now());
        auction.setUpdatedAt(LocalDateTime.now());
        return auction;
    }

    public void autoConfirmNow(Long orderId) {
        int updated = jdbcTemplate.update(
                "UPDATE \"order\" SET updated_at = NOW() - INTERVAL '10 days' WHERE order_id = ? AND status = 'DELIEVERED'",
                orderId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderExpiryTxService.confirmOne(orderId, LocalDateTime.now().minusDays(1));
    }

    public void expireNow(Long orderId) {
        int updated = jdbcTemplate.update(
                "UPDATE \"order\" SET payment_due = NOW() - INTERVAL '1 minute' WHERE order_id = ? AND status = 'PENDING'",
                orderId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderExpiryTxService.expireOne(orderId);
    }
}
