package com.b101.dib.auth.token;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenVerifier implements AccessTokenVerifier {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties properties;
    private final Clock clock;

    @Override
    public AccessTokenClaims verifyBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw unauthorized();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw unauthorized();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(
                            properties.secret().getBytes(StandardCharsets.UTF_8)
                    ))
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long memberId = Long.valueOf(claims.getSubject());
            MemberRole role = MemberRole.valueOf(claims.get("role", String.class));
            return new AccessTokenClaims(memberId, role);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw unauthorized();
        }
    }

    private BusinessException unauthorized() {
        return new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}

