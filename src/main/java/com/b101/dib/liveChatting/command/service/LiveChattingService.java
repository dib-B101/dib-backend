package com.b101.dib.liveChatting.command.service;

import com.b101.dib.liveChatting.command.dto.LiveChattingCommandDto;
import com.b101.dib.liveChatting.domain.LiveChatting;

public interface LiveChattingService {

	LiveChatting create(Long myId, Long liveBroadcastId, LiveChattingCommandDto dto);

}
