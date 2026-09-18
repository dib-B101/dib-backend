package com.b101.dib.auth.external.kakao;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;

import com.b101.dib.auth.config.KakaoProperties;
import com.b101.dib.auth.domain.PhoneNumber;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class KakaoRestOAuthClient implements KakaoOAuthClient {

    private final KakaoProperties properties;
    private final RestClient restClient;

    public KakaoRestOAuthClient(KakaoProperties properties) {
        this(properties, RestClient.create());
    }

    KakaoRestOAuthClient(KakaoProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public KakaoProfile authenticate(String authorizationCode, String redirectUri) {
        validateConfiguration();
        try {
            KakaoTokenResponse token = requestToken(authorizationCode, redirectUri);
            KakaoUserResponse user = requestUser(token.accessToken());
            return toProfile(user);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.warn("Kakao OAuth request failed: {}", exception.getMessage());
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    private KakaoTokenResponse requestToken(String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);
        if (hasText(properties.clientSecret())) {
            form.add("client_secret", properties.clientSecret());
        }

        KakaoTokenResponse response = restClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);
        if (response == null || !hasText(response.accessToken())) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
        return response;
    }

    private KakaoUserResponse requestUser(String accessToken) {
        KakaoUserResponse response = restClient.get()
                .uri(properties.userInfoUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);
        if (response == null || response.id() == null || response.account() == null) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
        return response;
    }

    private KakaoProfile toProfile(KakaoUserResponse user) {
        KakaoAccount account = user.account();
        if (Boolean.TRUE.equals(account.emailNeedsAgreement()) || !hasText(account.email())) {
            throw new BusinessException(ErrorCode.EMAIL_CONSENT_REQUIRED);
        }

        Profile profile = account.profile();
        String nickname = profile == null ? null : profile.nickname();
        String imageUrl = profile == null ? null : profile.profileImageUrl();
        Gender gender = parseGender(account.gender());
        LocalDate birthDate = parseBirthDate(account.birthyear(), account.birthday());
        String phoneNumber = normalizeKakaoPhone(account.phoneNumber());
        if (!hasText(nickname) || !hasText(account.name()) || gender == null
                || birthDate == null || phoneNumber == null) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }

        return new KakaoProfile(
                String.valueOf(user.id()),
                account.email().trim().toLowerCase(Locale.ROOT),
                nickname.trim(),
                imageUrl,
                account.name().trim(),
                gender,
                birthDate,
                phoneNumber
        );
    }

    private Gender parseGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return Gender.MALE;
        }
        if ("female".equalsIgnoreCase(gender)) {
            return Gender.FEMALE;
        }
        return null;
    }

    private LocalDate parseBirthDate(String birthyear, String birthday) {
        if (!hasText(birthyear) || birthday == null || !birthday.matches("\\d{4}")) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(birthyear),
                    Integer.parseInt(birthday.substring(0, 2)),
                    Integer.parseInt(birthday.substring(2, 4))
            );
        } catch (DateTimeException | NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeKakaoPhone(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return null;
        }
        String domestic = phoneNumber.trim().replaceFirst("^\\+82\\s*", "0");
        try {
            return PhoneNumber.from(domestic).value();
        } catch (BusinessException exception) {
            return null;
        }
    }

    private void validateConfiguration() {
        if (!hasText(properties.clientId()) || !hasText(properties.tokenUrl())
                || !hasText(properties.userInfoUrl())) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount account) {
    }

    record KakaoAccount(
            @JsonProperty("email_needs_agreement") Boolean emailNeedsAgreement,
            String email,
            Profile profile,
            String name,
            String gender,
            String birthyear,
            String birthday,
            @JsonProperty("phone_number") String phoneNumber
    ) {
    }

    record Profile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {
    }
}
