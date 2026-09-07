package com.b101.dib.live.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiveKitTokenResponse {
	String serverUrl;
	String token;
	String roomName;
}
