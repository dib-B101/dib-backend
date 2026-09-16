package com.b101.dib.order.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;

public interface MemberPurchaseQueryService {
    /** 특정 회원의 구매 및 낙찰 내역을 조회한다. */
    CursorPageDto<PurchaseHistoryQueryDto> findMine(
            Long memberId,
            OrderStatus orderStatus,
            String cursor,
            int size
    );
}
