package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.b101.dib.auth.command.dto.LoginRequest;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.flyway.locations=classpath:db/migration",
        "livekit.url=http://localhost:7880",
        "livekit.api-key=test-key",
        "livekit.api-secret=test-secret-test-secret-test-secret"
})
@Import(LoginServicePersistenceIntegrationTest.FixedClockConfig.class)
@Testcontainers
class LoginServicePersistenceIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-09-11T01:00:00Z");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg18")
                    .asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoginService loginService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private TokenIssuer tokenIssuer;
    @MockitoBean
    private RefreshSessionStore refreshSessionStore;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void persistsLastLoginAtAndUpdatedAt() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("Password1!"))
                .nickname("길동이")
                .name("홍길동")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("2000-01-01"))
                .phoneNumber("01012345678")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .score(50.0)
                .createdAt(LocalDateTime.ofInstant(NOW.minusSeconds(60), ZoneOffset.UTC))
                .updatedAt(LocalDateTime.ofInstant(NOW.minusSeconds(60), ZoneOffset.UTC))
                .warningCount(0)
                .build());
        AuthTokenPair tokens = new AuthTokenPair(
                "access-token",
                "refresh-token",
                "refresh-token-hash",
                "family-id",
                NOW,
                NOW.plus(Duration.ofDays(90))
        );
        given(tokenIssuer.issue(any(Member.class))).willReturn(tokens);

        loginService.login(new LoginRequest(
                "user@example.com", "Password1!", "device-id"
        ));
        entityManager.clear();

        Member reloadedMember = memberRepository.findById(member.getId()).orElseThrow();
        LocalDateTime expected = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        assertThat(reloadedMember.getLastLoginAt()).isEqualTo(expected);
        assertThat(reloadedMember.getUpdatedAt()).isEqualTo(expected);
        verify(refreshSessionStore).save(member.getId(), "device-id", tokens);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
