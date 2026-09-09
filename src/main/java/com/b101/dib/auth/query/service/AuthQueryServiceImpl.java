package com.b101.dib.auth.query.service;

import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.repository.AuthQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryServiceImpl implements AuthQueryService {

    private final AuthQueryMapper authQueryMapper;

    @Override
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        return new EmailAvailabilityResponse(!authQueryMapper.existsByEmail(email));
    }
}
