package com.b101.dib.settlement.payout;

import com.b101.dib.settlement.domain.Settlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class LoggingPayoutClient implements PayoutClient {
    @Override
    public String payout(Settlement s) {
        String txId = "PAYOUT-" + UUID.randomUUID();
        log.info("[정산 지급] settlementId={} sellerId={} {} {} net={}원 txId={}",
                s.getSettlementId(), s.getSellerId(), s.getBankName(), s.getAccountNumber(), s.getNetAmount(), txId);
        return txId;
    }
}
