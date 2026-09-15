package com.b101.dib.chatting.command.service;

import com.b101.dib.chatting.domain.Chatting;

public interface ChattingCommandService {
    Chatting send(Long memberId, Long orderId, String content);
}
