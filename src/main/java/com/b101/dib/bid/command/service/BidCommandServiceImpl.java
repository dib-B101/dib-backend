package com.b101.dib.bid.command.service;

import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.common.lock.AuctionLock;
import com.b101.dib.common.validation.TradeInputValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Redis 분산락(lock:auction:{id}) 을 잡고 입찰 트랜잭션(BidTxService) 을 실행한다.
// 락 획득 메서드와 @Transactional 메서드를 분리해야 커밋 뒤에 락이 풀린다 (같은 클래스 self-invocation 금지)
@Service
@RequiredArgsConstructor
public class BidCommandServiceImpl implements BidCommandService {
    private final BidTxService bidTxService;
    private final AuctionLock auctionLock;

    @Override
    public BidPlacedDto place(Long auctionId, Long memberId, Long amount) {
        TradeInputValidator.validatePrice(amount);
        return auctionLock.run(auctionId, () -> bidTxService.place(auctionId, memberId, amount));
    }
}
