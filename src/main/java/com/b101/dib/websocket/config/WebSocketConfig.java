package com.b101.dib.websocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	public static final long HEARTBEAT_MS = 10_000;

	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 서버 → 클라이언트. /topic 브로드캐스트, /queue 는 /user/queue/... 개인 메시지
		// heart-beat 10s/10s: 클라이언트(안드로이드)가 끊긴 연결을 30초 안에 알아채고 재연결 + Snapshot 으로 복구
		ThreadPoolTaskScheduler heartbeatScheduler = new ThreadPoolTaskScheduler();
		heartbeatScheduler.setPoolSize(1);
		heartbeatScheduler.setThreadNamePrefix("ws-heartbeat-");
		heartbeatScheduler.initialize();
		registry.enableSimpleBroker("/topic", "/queue")
				.setHeartbeatValue(new long[]{HEARTBEAT_MS, HEARTBEAT_MS})
				.setTaskScheduler(heartbeatScheduler);
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
