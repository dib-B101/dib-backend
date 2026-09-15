package com.b101.dib.auth.token;

import java.nio.charset.StandardCharsets;
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
    private final RefreshTokenHasher refreshTokenHasher;

    @Override
    public AuthTokenPair issue(Member member) {
        Instant issuedAt = clock.instant();
        Instant absoluteExpiresAt = issuedAt.plus(properties.refreshTokenAbsolute());
        return issue(member, UUID.randomUUID().toString(), issuedAt, absoluteExpiresAt);
    }

    @Override
    public AuthTokenPair rotate(Member member, String familyId, Instant absoluteExpiresAt) {
        Instant issuedAt = clock.instant();
        return issue(member, familyId, issuedAt, absoluteExpiresAt);
    }

    private AuthTokenPair issue(
            Member member,
            String familyId,
            Instant issuedAt,
            Instant absoluteExpiresAt
    ) {
        Instant accessExpiresAt = issuedAt.plusSeconds(properties.accessTokenValiditySeconds());
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
                refreshTokenHasher.hash(refreshToken),
                familyId,
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

}
