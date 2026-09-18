package com.b101.dib.liveBroadcast.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.liveBroadcast.command.dto.LiveItemRequest;
import com.b101.dib.liveBroadcast.command.dto.UpdateRequest;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;

import java.util.List;

public interface LiveBroadcastCommandService {

	LiveBroadcast create(Long myId, CreateRequest request);

	LiveBroadcast update(Long myId, Long liveBroadcastId, UpdateRequest request);

	LiveBroadcast delete(Long myId, Long liveBroadcastId);

	LiveBroadcast start(Long myId, Long liveBroadcastId);

	LiveBroadcast end(Long myId, Long liveBroadcastId);

	Auction startLiveAuction(Long myId, Long liveBroadcastId, Long auctionId);

	List<Auction> setItems(Long myId, Long liveBroadcastId, List<LiveItemRequest> items);

}
