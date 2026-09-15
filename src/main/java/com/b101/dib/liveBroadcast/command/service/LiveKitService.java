package com.b101.dib.liveBroadcast.command.service;

public interface LiveKitService {

	String createToken(String roomName, String identity, boolean b);

	String getLivekitUrl();

}
