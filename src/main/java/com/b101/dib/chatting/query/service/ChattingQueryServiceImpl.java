package com.b101.dib.chatting.query.service;

import com.b101.dib.chatting.query.dto.ChattingPageDto;
import com.b101.dib.chatting.query.dto.ChattingQueryDto;
import com.b101.dib.chatting.repository.ChattingMapper;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChattingQueryServiceImpl implements ChattingQueryService {
    private static final int MAX_SIZE = 100;

    private final ChattingMapper chattingMapper;
    private final OrderRepository orderRepository;

    @Override
    public ChattingPageDto findMessages(Long memberId, Long orderId, Long beforeChattingId, int size) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isParticipant(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        int limit = Math.max(1, Math.min(size, MAX_SIZE));
        List<ChattingQueryDto> rows = chattingMapper.findByOrderId(orderId, beforeChattingId, limit + 1);
        boolean hasMore = rows.size() > limit;
        if (hasMore) {
            rows = rows.subList(0, limit);
        }
        Collections.reverse(rows);

        ChattingPageDto page = new ChattingPageDto();
        page.setItems(rows);
        page.setHasMore(hasMore);
        page.setChattingReadOnly(order.isClosed());
        return page;
    }
}
