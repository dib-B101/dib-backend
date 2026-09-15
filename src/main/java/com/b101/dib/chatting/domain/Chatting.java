package com.b101.dib.chatting.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatting")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chatting {
    public static final int CONTENT_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chattingId;

    private Long orderId;
    private Long memberId;
    private String content;
    private LocalDateTime time;

    public static Chatting send(Order order, Long memberId, String content) {
        if (!order.isParticipant(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (order.isClosed()) {
            throw new BusinessException(ErrorCode.CHATTING_CLOSED);
        }
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty() || trimmed.length() > CONTENT_MAX) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return Chatting.builder()
                .orderId(order.getOrderId())
                .memberId(memberId)
                .content(trimmed)
                .time(LocalDateTime.now())
                .build();
    }
}
