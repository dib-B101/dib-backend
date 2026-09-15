package com.b101.dib.common.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.auth.token.JwtAuthenticationFilter;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AccessTokenVerifier accessTokenVerifier;
    private final MemberRepository memberRepository;
    private final boolean headerFallback;

    public SecurityConfig(AccessTokenVerifier accessTokenVerifier, MemberRepository memberRepository,
                          @Value("${dib.auth.header-fallback:false}") boolean headerFallback) {
        this.accessTokenVerifier = accessTokenVerifier;
        this.memberRepository = memberRepository;
        this.headerFallback = headerFallback;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> writeError(response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, e) -> writeError(response, ErrorCode.FORBIDDEN))
                )
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 열어두는 것
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/webhooks/**").permitAll()          // Toss 웹훅 (서명 검증은 서비스에서)
                        .requestMatchers("/api/v1/internal/**").permitAll()          // 서버 내부 호출 — 배포 시 네트워크로 차단
                        .requestMatchers("/api/v1/dev/**").permitAll()               // devtools (local 프로필에만 존재)
                        .requestMatchers("/api/v1/carriers", "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/auctions/**",
                                "/api/v1/live-broadcasts/**", "/api/v1/bookmarks").permitAll()
                        .requestMatchers("/ws/**", "/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // 관리자
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(accessTokenVerifier, memberRepository, headerFallback),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"" + errorCode.name() + "\",\"message\":\"" + errorCode.getMessage() + "\"}");
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
