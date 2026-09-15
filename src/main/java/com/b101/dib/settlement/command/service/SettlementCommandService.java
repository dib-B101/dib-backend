package com.b101.dib.settlement.command.service;

import com.b101.dib.order.domain.Order;
import com.b101.dib.settlement.domain.Settlement;

public interface SettlementCommandService {
    Settlement createFor(Order order);
    Settlement execute(Long settlementId);
}
