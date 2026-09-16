package com.b101.dib.websocket.config;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// STOMP CONNECT 의 Authorization: Bearer 를 검증해 세션 Principal(=memberId) 로 둔다.
// local 은 HTTP 필터와 같은 폴백: X-Member-Id 헤더만으로 인증
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private static final Pattern ORDER_TOPIC = Pattern.compile("^/topic/orders/(\\d+)$");

    private final AccessTokenVerifier accessTokenVerifier;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final boolean headerFallback;

    public StompAuthChannelInterceptor(AccessTokenVerifier accessTokenVerifier, MemberRepository memberRepository,
                                       OrderRepository orderRepository,
                                       @Value("${dib.auth.header-fallback:false}") boolean headerFallback) {
        this.accessTokenVerifier = accessTokenVerifier;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.headerFallback = headerFallback;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            checkSubscribe(accessor);
            return message;
        }
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null) {
            try {
                AccessTokenClaims claims = accessTokenVerifier.verifyBearer(authorization);
                accessor.setUser(authentication(claims.memberId(), claims.role().name()));
            } catch (BusinessException e) {
                throw new IllegalArgumentException("UNAUTHORIZED");   // CONNECT 거절 → 클라이언트에 ERROR 프레임
            }
            return message;
        }
        String header = accessor.getFirstNativeHeader("X-Member-Id");
        if (headerFallback && header != null) {
            Member member = memberRepository.findById(Long.valueOf(header.trim())).orElse(null);
            if (member != null) {
                accessor.setUser(authentication(member.getId(), member.getRole().name()));
            }
        }
        return message;   // 비로그인도 연결은 허용 (구독만 가능, 입찰은 무시)
    }

    // 주문 채팅 토픽은 그 주문의 구매자/판매자만, /user/** 개인 큐는 로그인만
    private void checkSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        Long memberId = accessor.getUser() == null ? null : parse(accessor.getUser().getName());
        if (destination.startsWith("/user/") && memberId == null) {
            throw new IllegalArgumentException("UNAUTHORIZED");
        }
        Matcher m = ORDER_TOPIC.matcher(destination);
        if (m.matches()) {
            Order order = orderRepository.findById(Long.valueOf(m.group(1))).orElse(null);
            if (memberId == null || order == null || !order.isParticipant(memberId)) {
                throw new IllegalArgumentException("FORBIDDEN");
            }
        }
    }

    private static Long parse(String name) {
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static UsernamePasswordAuthenticationToken authentication(Long memberId, String role) {
        return new UsernamePasswordAuthenticationToken(String.valueOf(memberId), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
