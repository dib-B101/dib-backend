package com.b101.dib.auth.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Base64;
import java.util.UUID;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenIssuerTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final Instant NOW = Instant.parse("2026-09-09T09:00:00Z");

    @Test
    void issuesAccessJwtAndOpaqueRefreshToken() {
        JwtProperties properties = new JwtProperties(
                SECRET, 1800, Duration.ofDays(30), Duration.ofDays(90)
        );
        JwtTokenIssuer issuer = new JwtTokenIssuer(
                properties,
                new SecureRandom(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        Member member = Member.builder()
                .id(1L)
                .role(MemberRole.USER)
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("2000-01-01"))
                .build();

        AuthTokenPair tokens = issuer.issue(member);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .clock(() -> Date.from(NOW))
                .build()
                .parseSignedClaims(tokens.accessToken())
                .getPayload();
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plusSeconds(1800));
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.refreshTokenHash()).isEqualTo(sha256(tokens.refreshToken()));
        assertThat(tokens.familyId()).satisfies(value -> UUID.fromString(value));
        assertThat(tokens.absoluteExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(90)));

        AuthTokenPair nextTokens = issuer.issue(member);
        assertThat(nextTokens.refreshToken()).isNotEqualTo(tokens.refreshToken());
        assertThat(nextTokens.familyId()).isNotEqualTo(tokens.familyId());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
