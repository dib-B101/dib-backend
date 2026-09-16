package com.b101.dib.liveChatting.query.service;

import java.util.List;

import com.b101.dib.liveChatting.query.dto.LiveChattingQueryDto;

public interface LiveChattingQueryService {

	List<LiveChattingQueryDto> findByLiveBroadcastId(Long liveBroadcastId);

}
