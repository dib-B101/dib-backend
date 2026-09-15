package com.b101.dib.websocket.config;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
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

// STOMP CONNECT 의 Authorization: Bearer 를 검증해 세션 Principal(=memberId) 로 둔다.
// local 은 HTTP 필터와 같은 폴백: X-Member-Id 헤더만으로 인증
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final AccessTokenVerifier accessTokenVerifier;
    private final MemberRepository memberRepository;
    private final boolean headerFallback;

    public StompAuthChannelInterceptor(AccessTokenVerifier accessTokenVerifier, MemberRepository memberRepository,
                                       @Value("${dib.auth.header-fallback:false}") boolean headerFallback) {
        this.accessTokenVerifier = accessTokenVerifier;
        this.memberRepository = memberRepository;
        this.headerFallback = headerFallback;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
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

    private static UsernamePasswordAuthenticationToken authentication(Long memberId, String role) {
        return new UsernamePasswordAuthenticationToken(String.valueOf(memberId), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
