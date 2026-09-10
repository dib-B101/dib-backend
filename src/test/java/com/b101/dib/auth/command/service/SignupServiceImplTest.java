package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.b101.dib.auth.command.dto.SignupRequest;
import com.b101.dib.auth.command.dto.SignupResponse;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SignupServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-09T09:00:00Z");

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PhoneVerificationService phoneVerificationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenIssuer tokenIssuer;
    @Mock
    private RefreshSessionStore refreshSessionStore;

    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupServiceImpl(
                memberRepository,
                phoneVerificationService,
                passwordEncoder,
                tokenIssuer,
                refreshSessionStore,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void signsUpMemberAndIssuesTokens() {
        SignupRequest request = validRequest();
        Member savedMember = savedMember();
        AuthTokenPair tokens = tokens();
        given(passwordEncoder.encode("Password1!")).willReturn("encoded-password");
        given(memberRepository.saveAndFlush(any(Member.class))).willReturn(savedMember);
        given(tokenIssuer.issue(savedMember)).willReturn(tokens);

        SignupResponse response = signupService.signup(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        Member newMember = memberCaptor.getValue();
        assertThat(newMember.getEmail()).isEqualTo("user@example.com");
        assertThat(newMember.getPassword()).isEqualTo("encoded-password");
        assertThat(newMember.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(newMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(newMember.getRole()).isEqualTo(MemberRole.USER);
        assertThat(newMember.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        );
        verify(refreshSessionStore).save(1L, "device-id", tokens);
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void rejectsInvalidPasswordBeforeConsumingVerification() {
        SignupRequest request = new SignupRequest(
                "user@example.com", "weak-password", "홍길동", "길동이",
                Gender.MALE, LocalDate.parse("2000-01-01"), "01012345678",
                "verification-token", "device-id"
        );

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_PASSWORD));

        verifyNoInteractions(phoneVerificationService, passwordEncoder, tokenIssuer, refreshSessionStore);
    }

    @Test
    void doesNotTreatKoreanLetterAsPasswordSpecialCharacter() {
        SignupRequest request = new SignupRequest(
                "user@example.com", "Password1가", "홍길동", "길동이",
                Gender.MALE, LocalDate.parse("2000-01-01"), "01012345678",
                "verification-token", "device-id"
        );

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_PASSWORD));

        verifyNoInteractions(phoneVerificationService, passwordEncoder, tokenIssuer, refreshSessionStore);
    }

    @Test
    void rejectsDuplicatedEmailBeforeConsumingVerification() {
        given(memberRepository.existsByEmail("user@example.com")).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.EMAIL_DUPLICATED));

        verifyNoInteractions(phoneVerificationService, passwordEncoder, tokenIssuer, refreshSessionStore);
    }

    @Test
    void rejectsDuplicatedNicknameBeforeConsumingVerification() {
        given(memberRepository.existsByNickname("길동이")).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.NICKNAME_DUPLICATED));

        verifyNoInteractions(phoneVerificationService, passwordEncoder, tokenIssuer, refreshSessionStore);
    }

    @Test
    void rejectsDuplicatedPhoneNumberBeforeConsumingVerification() {
        given(memberRepository.existsByPhoneNumber("01012345678")).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PHONE_DUPLICATED));

        verifyNoInteractions(phoneVerificationService, passwordEncoder, tokenIssuer, refreshSessionStore);
    }

    @Test
    void mapsConcurrentPhoneConflictToPhoneDuplicated() {
        given(passwordEncoder.encode("Password1!")).willReturn("encoded-password");
        given(memberRepository.saveAndFlush(any(Member.class)))
                .willThrow(new DataIntegrityViolationException(
                        "constraint violation",
                        new RuntimeException("uq_member_phone_number")
                ));

        assertThatThrownBy(() -> signupService.signup(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.PHONE_DUPLICATED));
    }

    private SignupRequest validRequest() {
        return new SignupRequest(
                " User@Example.com ", "Password1!", " 홍길동 ", " 길동이 ",
                Gender.MALE, LocalDate.parse("2000-01-01"), "010-1234-5678",
                "verification-token", " device-id "
        );
    }

    private Member savedMember() {
        return Member.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("길동이")
                .name("홍길동")
                .gender(Gender.MALE)
                .birthDate(LocalDate.parse("2000-01-01"))
                .phoneNumber("01012345678")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .score(50.0)
                .createdAt(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC))
                .updatedAt(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC))
                .warningCount(0)
                .build();
    }

    private AuthTokenPair tokens() {
        return new AuthTokenPair(
                "access-token", "refresh-token", "refresh-hash", "family-id",
                NOW, NOW.plusSeconds(90L * 24 * 60 * 60)
        );
    }
}
