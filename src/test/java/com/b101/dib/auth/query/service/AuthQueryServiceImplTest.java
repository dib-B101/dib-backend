package com.b101.dib.auth.query.service;

import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.dto.EmailLookupResponse;
import com.b101.dib.auth.query.repository.AuthQueryMapper;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthQueryServiceImplTest {

    @Mock
    private AuthQueryMapper authQueryMapper;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @InjectMocks
    private AuthQueryServiceImpl authQueryService;

    @Test
    void returnsAvailableWhenEmailDoesNotExist() {
        given(authQueryMapper.existsByEmail("new@example.com")).willReturn(false);

        EmailAvailabilityResponse response =
                authQueryService.checkEmailAvailability("new@example.com");

        assertThat(response.available()).isTrue();
    }

    @Test
    void returnsUnavailableWhenEmailAlreadyExists() {
        given(authQueryMapper.existsByEmail("member@example.com")).willReturn(true);

        EmailAvailabilityResponse response =
                authQueryService.checkEmailAvailability("member@example.com");

        assertThat(response.available()).isFalse();
    }

    @Test
    void consumesVerificationAndReturnsMaskedEmail() {
        given(authQueryMapper.findEmailByPhoneNumber("01012345678"))
                .willReturn("jihun123@example.com");

        EmailLookupResponse response = authQueryService.findEmail(
                "verification-token",
                " 010-1234-5678 "
        );

        assertThat(response.maskedEmail()).isEqualTo("j*****@example.com");
        var inOrder = inOrder(phoneVerificationService, authQueryMapper);
        inOrder.verify(phoneVerificationService).consumeVerificationToken(
                "verification-token",
                PhoneVerificationPurpose.FIND_EMAIL,
                "01012345678"
        );
        inOrder.verify(authQueryMapper).findEmailByPhoneNumber("01012345678");
    }

    @Test
    void throwsMemberNotFoundAfterConsumingVerification() {
        given(authQueryMapper.findEmailByPhoneNumber("01012345678")).willReturn(null);

        assertThatThrownBy(() -> authQueryService.findEmail(
                "verification-token",
                "01012345678"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token",
                PhoneVerificationPurpose.FIND_EMAIL,
                "01012345678"
        );
        verify(authQueryMapper).findEmailByPhoneNumber("01012345678");
    }

    @Test
    void masksOneCharacterLocalPartWithoutRevealingItsLength() {
        given(authQueryMapper.findEmailByPhoneNumber("01012345678"))
                .willReturn("a@example.com");

        EmailLookupResponse response = authQueryService.findEmail(
                "verification-token",
                "01012345678"
        );

        assertThat(response.maskedEmail()).isEqualTo("a*****@example.com");
    }

    @Test
    void doesNotLookUpMemberWhenVerificationIsInvalid() {
        willThrow(new BusinessException(ErrorCode.INVALID_VERIFICATION))
                .given(phoneVerificationService)
                .consumeVerificationToken(
                        "invalid-token",
                        PhoneVerificationPurpose.FIND_EMAIL,
                        "01012345678"
                );

        assertThatThrownBy(() -> authQueryService.findEmail(
                "invalid-token",
                "01012345678"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_VERIFICATION));

        verifyNoInteractions(authQueryMapper);
    }
}
