package com.b101.dib.liveChatting.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b101.dib.liveChatting.query.dto.LiveChattingQueryDto;
import com.b101.dib.liveChatting.repository.LiveChattingMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveChattingQueryServiceImpl implements LiveChattingQueryService {
	
	private final LiveChattingMapper liveChattingMapper;
	
	@Override
	public List<LiveChattingQueryDto> findByLiveBroadcastId(Long liveBroadcastId) {
		return liveChattingMapper.findByLiveBroadcastId(liveBroadcastId);
	}

}
