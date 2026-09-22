package com.b101.dib.liveChatting.command.service;

import org.springframework.stereotype.Service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.liveChatting.command.dto.LiveChattingCommandDto;
import com.b101.dib.liveChatting.domain.LiveChatting;
import com.b101.dib.liveChatting.repository.LiveChattingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveChattingServiceImpl implements LiveChattingService {
	
	private final LiveChattingRepository liveChattingRepository;
	
	@Override
	public LiveChatting create(Long myId, Long liveBroadcastId,LiveChattingCommandDto dto) {
		LiveChatting liveChatting = LiveChatting.builder()
				.liveBroadcastId(liveBroadcastId)
				.memberId(myId)
				.content(dto.getContent())
				.time(dto.getTime())
				.build();
		return liveChattingRepository.save(liveChatting);
	}

}
