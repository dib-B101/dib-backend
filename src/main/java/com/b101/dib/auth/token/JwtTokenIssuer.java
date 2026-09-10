package com.b101.dib.auth.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.member.domain.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    public AuthTokenPair issue(Member member) {
        Instant issuedAt = clock.instant();
        Instant accessExpiresAt = issuedAt.plusSeconds(properties.accessTokenValiditySeconds());
        Instant absoluteExpiresAt = issuedAt.plus(properties.refreshTokenAbsolute());
        String accessToken = Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(accessExpiresAt))
                .signWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();
        String refreshToken = randomToken();

        return new AuthTokenPair(
                accessToken,
                refreshToken,
                sha256(refreshToken),
                UUID.randomUUID().toString(),
                issuedAt,
                absoluteExpiresAt
        );
    }

    // 랜덤 토큰 생성
    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 해시 생성
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Refresh Token 해시를 생성할 수 없습니다.", e);
        }
    }
}
