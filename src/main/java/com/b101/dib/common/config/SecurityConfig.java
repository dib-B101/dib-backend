package com.b101.dib.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.b101.dib.auth.security.JwtAuthenticationFilter;
import com.b101.dib.auth.security.RestAuthenticationEntryPoint;
import com.b101.dib.auth.token.AccessTokenVerifier;

import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new RestAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            AccessTokenVerifier accessTokenVerifier,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        return new JwtAuthenticationFilter(accessTokenVerifier, restAuthenticationEntryPoint);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            MemberIdHeaderFilter memberIdHeaderFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(restAuthenticationEntryPoint)
                )

                .authorizeHttpRequests(auth -> auth
                        // 헬스는 ALB 대상 검사와 K8s probe 가 토큰 없이 친다. 이 둘만 공개
                        .requestMatchers("/actuator/health/**", "/actuator/info")
                        .permitAll()
                        // metrics·prometheus 는 엔드포인트 목록·요청량·JVM 내부를 그대로 노출한다.
                        // ALB 가 인터넷에 붙어 있으므로 공개하면 안 된다
                        .requestMatchers("/actuator/**")
                        .hasRole("ADMIN")
                        // 관리자 API 는 ADMIN 롤만. 이 줄이 없으면 아래 anyRequest().permitAll() 에 걸려 누구나 호출된다
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        // 운영/테스트용 내부 API. 주문 생성은 낙찰자 카드로 실제 결제까지 일으키고(OrderInternalController)
                        // 정산 실행은 판매자에게 돈을 내보낸다(InternalSettlementController). 회원 식별 헤더조차 안 받으므로
                        // permitAll 로 두면 ALB 주소만 알면 누구나 id 를 훑어가며 호출할 수 있다.
                        // (HMAC 으로 검증하는 AI 콜백은 /internal/v1/... 이라 여기 안 걸린다)
                        .requestMatchers("/api/v1/internal/**")
                        .hasRole("ADMIN")
                        // 아래 API 들은 X-Member-Id 를 필수로 받는다. permitAll 로 두면 비로그인 호출이
                        // 401 이 아니라 MissingRequestHeaderException 400 으로 나가서 앱이 재로그인 유도를 못 한다
                        .requestMatchers("/api/v1/members/me/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/notifications/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/questions/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/reports/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/settlements/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/bookmarks/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/*/reports")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auctions/*/orders/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auctions/*/bids")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me/addresses")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/me/addresses")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/members/me/addresses/{addressId}")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/members/me/addresses/{addressId}")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me/bids")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me/sales")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me/purchases")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/members/me/profile")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/me/withdrawal")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/products")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/members/me")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-broadcasts/*/token")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-broadcasts/*/chats")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-broadcasts/*/start")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-broadcasts/*/end")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/live-broadcasts/*/auctions/*/start")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/live-broadcasts/*/items")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/auctions/*/start")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/auctions/*/relist")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/auctions/**")
                        .hasAnyRole("USER", "ADMIN")
                        // ⚠ 기본 개방이다. 위 목록에 없는 새 엔드포인트는 자동으로 공개된다.
                        // 보호가 필요한 컨트롤러를 추가하면 반드시 위에 한 줄 같이 넣을 것.
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT claims를 기존 X-Member-Id 기반 컨트롤러에 전달하고, local 프로필에서는
                // 테스트 헤더 인증도 인가 전에 적용한다.
                .addFilterAfter(memberIdHeaderFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    // MemberIdHeaderFilter는 SecurityFilterChain 안에서만 한 번 실행한다.
    @Bean
    public FilterRegistrationBean<MemberIdHeaderFilter> disableMemberIdHeaderFilterServletRegistration(
            MemberIdHeaderFilter filter
    ) {
        FilterRegistrationBean<MemberIdHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        // 추후 쿠키/인증 정보를 사용할 경우 필요
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
