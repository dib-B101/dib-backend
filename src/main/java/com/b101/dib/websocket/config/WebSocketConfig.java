package com.b101.dib.websocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 서버 → 클라이언트. /topic 브로드캐스트, /queue 는 /user/queue/... 개인 메시지
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setUserDestinationPrefix("/user");

		// 클라이언트 → 서버
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompAuthChannelInterceptor);   // CONNECT 에서 JWT → Principal
	}
}
