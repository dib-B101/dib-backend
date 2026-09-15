package com.b101.dib.liveBroadcast.command.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.liveBroadcast.command.dto.UpdateRequest;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastRole;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.websocket.livekit.config.LiveKitConfig;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;
import com.b101.dib.websocket.livekit.service.LiveKitService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveBroadcastCommandServiceImpl implements LiveBroadcastCommandService {
	
	private final LiveBroadcastRepository liveBroadcastRepository;

	@Override
	public LiveBroadcast create(Long myId, CreateRequest request) {
		if(request.getTitle() == null) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NO_TITLE);
		}
		String roomName = "live-broadcast-" + UUID.randomUUID();
		LiveBroadcast liveBroadcast = LiveBroadcast.builder()
				.memberId(myId)
				.title(request.getTitle())
				.description(request.getDescription())
				.status(LiveBroadcastStatus.SCHEDULED)
				.livekitRoomName(roomName)
				.startedAt(request.getStartedAt())
				.endedAt(null)
				.viewCount(0)
				.createdAt(LocalDateTime.now())
				.updatedAt(null)
				.build();
		liveBroadcastRepository.save(liveBroadcast);
		return liveBroadcast;
	}

	@Override
	public LiveBroadcast update(Long myId, Long liveBroadcastId, UpdateRequest request) {
		LiveBroadcast liveBroadcast = checkLiveBroadcast(myId, liveBroadcastId);
		boolean updated = false;
		if(request.getTitle() != null) {
			liveBroadcast.setTitle(request.getTitle());
			updated = true;
		}
		if(request.getDescription() != null) {
			liveBroadcast.setDescription(request.getDescription());
			updated = true;
		}
		if(request.getStartedAt() != null) {
			liveBroadcast.setStartedAt(request.getStartedAt());
			updated = true;
		}
		if(updated) {
			liveBroadcast.setUpdatedAt(LocalDateTime.now());
		}
		return liveBroadcast;
	}

	@Override
	public LiveBroadcast delete(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = checkLiveBroadcast(myId, liveBroadcastId);
		liveBroadcastRepository.delete(liveBroadcast);
		return liveBroadcast;
	}
	
	private LiveBroadcast checkLiveBroadcast(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId)
				.orElseThrow(() -> new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND));
		if(!myId.equals(liveBroadcast.getMemberId())) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_OWNED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.LIVE) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_STARTED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.ENDED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_ENDED);
		}
		if(liveBroadcast.getStatus() == LiveBroadcastStatus.CANCELED) {
			throw new BusinessException(ErrorCode.LIVE_BROADCAST_ALREADY_CANCELED);
		}
		return liveBroadcast;
	}
}