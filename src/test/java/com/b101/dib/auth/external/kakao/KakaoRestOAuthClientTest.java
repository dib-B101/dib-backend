package com.b101.dib.auth.external.kakao;

import java.time.Duration;

import com.b101.dib.auth.config.KakaoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoRestOAuthClientTest {

    private MockRestServiceServer server;
    private KakaoRestOAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoRestOAuthClient(
                new KakaoProperties(
                        "client-id",
                        "client-secret",
                        "https://kauth.kakao.test/oauth/token",
                        "https://kapi.kakao.test/v2/user/me",
                        Duration.ofMinutes(10),
                        java.util.List.of("http://localhost/callback")
                ),
                builder.build()
        );
    }

    @Test
    void exchangesAuthorizationCodeAndReadsKakaoProfile() {
        server.expect(once(), requestTo("https://kauth.kakao.test/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("code=authorization-code")))
                .andRespond(withSuccess("{\"access_token\":\"kakao-access-token\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://kapi.kakao.test/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-access-token"))
                .andRespond(withSuccess("""
                        {
                          "id":12345,
                          "kakao_account":{
                            "profile":{
                              "nickname":"카카오닉네임",
                              "profile_image_url":"https://image.example/profile.jpg"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        KakaoProfile profile = client.authenticate(
                "authorization-code", "http://localhost/callback"
        );

        assertThat(profile.providerUserId()).isEqualTo("12345");
        assertThat(profile.nickname()).isEqualTo("카카오닉네임");
        assertThat(profile.profileImageUrl()).isEqualTo("https://image.example/profile.jpg");
        server.verify();
    }

    @Test
    void authenticatesWithOnlyKakaoUserId() {
        server.expect(requestTo("https://kauth.kakao.test/oauth/token"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.test/v2/user/me"))
                .andRespond(withSuccess("{\"id\":12345}", MediaType.APPLICATION_JSON));

        KakaoProfile profile = client.authenticate("code", "http://localhost/callback");

        assertThat(profile.providerUserId()).isEqualTo("12345");
        assertThat(profile.nickname()).isNull();
        assertThat(profile.profileImageUrl()).isNull();
    }
}
