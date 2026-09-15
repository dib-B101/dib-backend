package com.b101.dib.settlement.payout;

import com.b101.dib.settlement.domain.Settlement;

public interface PayoutClient {
    String payout(Settlement settlement);   // 반환: 외부 지급 거래 식별자
}
