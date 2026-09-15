package com.b101.dib.liveBroadcast.query.service;

import java.util.List;

import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;

public interface LiveBroadcastQueryService {

	List<LiveBroadcastQueryDto> findAll();

	LiveBroadcastQueryDto findById(Long liveBroadcastId);

	List<LiveBroadcastQueryDto> findByMemberId(Long memberId);

}
