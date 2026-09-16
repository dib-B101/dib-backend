package com.b101.dib.liveChatting.command.service;

import com.b101.dib.liveChatting.command.dto.LiveChattingCommandDto;

public interface LiveChattingService {

	void create(Long myId, Long liveBroadcastId, LiveChattingCommandDto dto);

}
