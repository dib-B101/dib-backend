package com.b101.dib.auth.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// 토큰에서 꺼낸 memberId 를 X-Member-Id 헤더로 덮어쓴다. 클라이언트가 보낸 X-Member-Id 는 무시
public class MemberIdHeaderRequest extends HttpServletRequestWrapper {
    private final String memberId;

    public MemberIdHeaderRequest(HttpServletRequest request, Long memberId) {
        super(request);
        this.memberId = String.valueOf(memberId);
    }

    @Override
    public String getHeader(String name) {
        if (JwtAuthenticationFilter.MEMBER_ID_HEADER.equalsIgnoreCase(name)) {
            return memberId;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (JwtAuthenticationFilter.MEMBER_ID_HEADER.equalsIgnoreCase(name)) {
            return Collections.enumeration(List.of(memberId));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
        if (names.stream().noneMatch(JwtAuthenticationFilter.MEMBER_ID_HEADER::equalsIgnoreCase)) {
            names.add(JwtAuthenticationFilter.MEMBER_ID_HEADER);
        }
        return Collections.enumeration(names);
    }
}
