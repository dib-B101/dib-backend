package com.b101.dib.liveBroadcast.command.service;

import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.liveBroadcast.command.dto.UpdateRequest;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;

public interface LiveBroadcastCommandService {

	LiveBroadcast create(Long myId, CreateRequest request);

	LiveBroadcast update(Long myId, Long liveBroadcastId, UpdateRequest request);

	LiveBroadcast delete(Long myId, Long liveBroadcastId);

}
