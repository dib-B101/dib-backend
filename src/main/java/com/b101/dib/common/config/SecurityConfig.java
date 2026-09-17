package com.b101.dib.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        // 관리자 API 는 ADMIN 롤만. 이 줄이 없으면 아래 anyRequest().permitAll() 에 걸려 누구나 호출된다
                        .requestMatchers("/api/v1/admin/**")
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
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
