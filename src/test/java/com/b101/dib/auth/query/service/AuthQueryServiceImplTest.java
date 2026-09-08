package com.b101.dib.auth.query.service;

import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.mapper.AuthQueryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthQueryServiceImplTest {

    @Mock
    private AuthQueryMapper authQueryMapper;

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
}
