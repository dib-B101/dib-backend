package com.b101.dib.auth.query.service;

import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.auth.domain.PhoneNumber;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.dto.EmailLookupResponse;
import com.b101.dib.auth.query.repository.AuthQueryMapper;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryServiceImpl implements AuthQueryService {

    private final AuthQueryMapper authQueryMapper;
    private final PhoneVerificationService phoneVerificationService;

    @Override
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        return new EmailAvailabilityResponse(!authQueryMapper.existsByEmail(email));
    }

    @Override
    public EmailLookupResponse findEmail(String verificationToken, String phoneNumber) {
        String normalizedPhoneNumber = PhoneNumber.from(phoneNumber).value();
        phoneVerificationService.consumeVerificationToken(
                verificationToken,
                PhoneVerificationPurpose.FIND_EMAIL,
                normalizedPhoneNumber
        );

        String email = authQueryMapper.findEmailByPhoneNumber(normalizedPhoneNumber);
        if (email == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return new EmailLookupResponse(maskEmail(email));
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            throw new IllegalStateException("회원 이메일 형식이 올바르지 않습니다.");
        }
        return email.substring(0, 1) + "*****" + email.substring(atIndex);
    }
}
