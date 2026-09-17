package com.b101.dib.auth.security;

import java.io.IOException;
import java.util.List;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_PREFIX = "ROLE_";
    // 토큰 재발급은 만료된 액세스 토큰을 들고 오는 게 정상이다. 여기서 401 을 내면 리프레시 토큰이
    // 살아 있어도 갱신이 막혀 사용자가 강제 로그아웃된다
    private static final String TOKEN_REFRESH_PATH = "/api/v1/auth/token/refresh";

    private final AccessTokenVerifier accessTokenVerifier;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (TOKEN_REFRESH_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (authorizationHeader == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AccessTokenClaims claims = accessTokenVerifier.verifyBearer(authorizationHeader);
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            claims,
                            null,
                            List.of(new SimpleGrantedAuthority(ROLE_PREFIX + claims.role().name()))
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(exception.getMessage(), exception)
            );
        }
    }
}
