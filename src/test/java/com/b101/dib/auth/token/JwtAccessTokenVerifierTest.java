package com.b101.dib.auth.token;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.MemberRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenVerifierTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final Instant NOW = Instant.parse("2026-09-11T02:00:00Z");

    @Test
    void verifiesBearerAccessToken() {
        JwtAccessTokenVerifier verifier = verifierAt(NOW);

        AccessTokenClaims claims = verifier.verifyBearer("Bearer " + token(
                NOW.minusSeconds(60),
                NOW.plusSeconds(60)
        ));

        assertThat(claims.memberId()).isEqualTo(1L);
        assertThat(claims.role()).isEqualTo(MemberRole.USER);
    }

    @Test
    void rejectsMissingMalformedAndExpiredTokens() {
        JwtAccessTokenVerifier verifier = verifierAt(NOW);

        assertUnauthorized(() -> verifier.verifyBearer(null));
        assertUnauthorized(() -> verifier.verifyBearer("Basic credentials"));
        assertUnauthorized(() -> verifier.verifyBearer("Bearer invalid-token"));
        assertUnauthorized(() -> verifier.verifyBearer("Bearer " + token(
                NOW.minusSeconds(120),
                NOW.minusSeconds(60)
        )));
    }

    private String token(Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject("1")
                .claim("role", "USER")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private JwtAccessTokenVerifier verifierAt(Instant instant) {
        return new JwtAccessTokenVerifier(
                new JwtProperties(SECRET, 1800, null, null),
                Clock.fixed(instant, ZoneOffset.UTC)
        );
    }

    private void assertUnauthorized(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}

