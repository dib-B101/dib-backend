package com.b101.dib.auth.token;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// SecurityConfig 에서 직접 생성해 필터 체인에 넣는다 (@Component 로 두면 서블릿 필터로 한 번 더 걸림)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String MEMBER_ID_HEADER = "X-Member-Id";

    private final AccessTokenVerifier accessTokenVerifier;
    private final MemberRepository memberRepository;
    private final boolean headerFallback;   // local 전용. 토큰 없이 X-Member-Id 만으로 인증 (Postman / flow_test)

    public JwtAuthenticationFilter(AccessTokenVerifier accessTokenVerifier, MemberRepository memberRepository, boolean headerFallback) {
        this.accessTokenVerifier = accessTokenVerifier;
        this.memberRepository = memberRepository;
        this.headerFallback = headerFallback;
    }

    // 인증 API 는 토큰이 만료돼 있어도 통과해야 한다 (refresh / logout 이 스스로 헤더를 읽음)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            AccessTokenClaims claims;
            try {
                claims = accessTokenVerifier.verifyBearer(authorization);
            } catch (BusinessException e) {
                writeError(response, ErrorCode.UNAUTHORIZED);
                return;
            }
            authenticate(claims.memberId(), claims.role());
            chain.doFilter(new MemberIdHeaderRequest(request, claims.memberId()), response);   // 컨트롤러는 그대로 X-Member-Id 를 읽는다
            return;
        }

        String header = request.getHeader(MEMBER_ID_HEADER);
        if (headerFallback && header != null) {
            Long memberId = parseMemberId(header);
            Member member = memberId == null ? null : memberRepository.findById(memberId).orElse(null);
            if (member == null) {
                writeError(response, ErrorCode.UNAUTHORIZED);
                return;
            }
            authenticate(member.getId(), member.getRole());
        }
        chain.doFilter(request, response);
    }

    private void authenticate(Long memberId, MemberRole role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                memberId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Long parseMemberId(String header) {
        try {
            return Long.valueOf(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"" + errorCode.name() + "\",\"message\":\"" + errorCode.getMessage() + "\"}");
    }
}
