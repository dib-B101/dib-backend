package com.b101.dib.chatting.command.service;

import com.b101.dib.chatting.domain.Chatting;
import com.b101.dib.chatting.repository.ChattingRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChattingCommandServiceImpl implements ChattingCommandService {
    private final ChattingRepository chattingRepository;
    private final OrderRepository orderRepository;

    @Override
    public Chatting send(Long memberId, Long orderId, String content) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return chattingRepository.save(Chatting.send(order, memberId, content));
    }
}
