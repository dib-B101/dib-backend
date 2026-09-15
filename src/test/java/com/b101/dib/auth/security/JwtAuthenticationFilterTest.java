package com.b101.dib.auth.security;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.MemberRole;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AccessTokenVerifier accessTokenVerifier;
    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuesWithoutAuthenticationWhenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(accessTokenVerifier, never()).verifyBearer(null);
    }

    @Test
    void registersMemberClaimsAndRoleWhenAccessTokenIsValid()
            throws ServletException, IOException {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(claims);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void returnsUnauthorizedWithoutContinuingWhenAccessTokenIsInvalid()
            throws ServletException, IOException {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        given(accessTokenVerifier.verifyBearer("Bearer invalid-token"))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationEntryPoint).commence(
                org.mockito.ArgumentMatchers.eq(request),
                org.mockito.ArgumentMatchers.eq(response),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private JwtAuthenticationFilter filter() {
        return new JwtAuthenticationFilter(accessTokenVerifier, authenticationEntryPoint);
    }
}
