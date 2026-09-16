package com.b101.dib.common.config;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 인증은 auth 의 JwtAuthenticationFilter(Security 체인)가 한다. 이 필터는 그 뒤(서블릿 필터)에서
// 인증된 memberId 를 X-Member-Id 헤더로 넣어 기존 컨트롤러가 그대로 동작하게 한다.
// 토큰이 없으면 클라이언트가 보낸 X-Member-Id 는 제거 (위조 방지). local 폴백(dib.auth.header-fallback=true)일 때만 그 값을 믿고
// 회원 DB 의 role 로 SecurityContext 도 채운다 → Postman / flow_test.
@Component
public class MemberIdHeaderFilter extends OncePerRequestFilter {
    private final ObjectProvider<MemberRepository> memberRepositoryProvider;
    private final boolean headerFallback;

    public MemberIdHeaderFilter(ObjectProvider<MemberRepository> memberRepositoryProvider,
                                @Value("${dib.auth.header-fallback:false}") boolean headerFallback) {
        this.memberRepositoryProvider = memberRepositoryProvider;
        this.headerFallback = headerFallback;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long memberId = authenticatedMemberId();
        if (memberId == null && headerFallback) {
            memberId = fallback(request.getHeader(MemberIdRequestWrapper.HEADER));
        }
        chain.doFilter(new MemberIdRequestWrapper(request, memberId), response);
    }

    private static Long authenticatedMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccessTokenClaims claims) {
            return claims.memberId();
        }
        return null;
    }

    private Long fallback(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        MemberRepository memberRepository = memberRepositoryProvider.getIfAvailable();
        if (memberRepository == null) {
            return null;
        }
        Long id;
        try {
            id = Long.valueOf(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            return null;
        }
        AccessTokenClaims claims = new AccessTokenClaims(member.getId(), member.getRole());
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                claims, null, List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))));
        return member.getId();
    }
}
