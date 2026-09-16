package com.b101.dib.order.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;
import com.b101.dib.order.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPurchaseQueryServiceImpl implements MemberPurchaseQueryService {
    private final OrderMapper orderMapper;

    @Override
    public CursorPageDto<PurchaseHistoryQueryDto> findMine(
            Long memberId,
            OrderStatus orderStatus,
            String cursor,
            int size
    ) {
        int limit = CursorPageDto.limit(size);
        List<PurchaseHistoryQueryDto> rows = orderMapper.findPurchasesByBuyerId(
                        memberId,
                        orderStatus,
                        CursorPageDto.parseCursor(cursor),
                        limit + 1
                ).stream()
                .map(PurchaseHistoryQueryDto::from)
                .toList();

        return CursorPageDto.of(
                rows,
                limit,
                row -> row.getOrder().getOrderId()
        );
    }
}
