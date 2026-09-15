package com.b101.dib.liveBroadcast.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveBroadcastQueryServiceImpl implements LiveBroadcastQueryService {
	
	private final LiveBroadcastMapper liveBroadcastMapper;

	@Override
	public List<LiveBroadcastQueryDto> findAll() {
		return liveBroadcastMapper.findAll();
	}

	@Override
	public LiveBroadcastQueryDto findById(Long liveBroadcastId) {
		LiveBroadcastQueryDto dto = liveBroadcastMapper.findById(liveBroadcastId);
		if(dto == null) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND);
		}
		return dto;
	}

	@Override
	public List<LiveBroadcastQueryDto> findByMemberId(Long memberId) {
		return liveBroadcastMapper.findByMemberId(memberId);
	}
}
