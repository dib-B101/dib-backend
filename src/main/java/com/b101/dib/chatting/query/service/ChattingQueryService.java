package com.b101.dib.chatting.query.service;

import com.b101.dib.chatting.query.dto.ChattingPageDto;

public interface ChattingQueryService {
    ChattingPageDto findMessages(Long memberId, Long orderId, Long beforeChattingId, int size);
}
