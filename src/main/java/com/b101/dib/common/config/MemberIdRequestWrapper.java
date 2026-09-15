package com.b101.dib.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// X-Member-Id 헤더를 서버가 정한 값으로 고정한다 (null 이면 헤더 제거). 클라이언트가 보낸 값은 무시
public class MemberIdRequestWrapper extends HttpServletRequestWrapper {
    public static final String HEADER = "X-Member-Id";

    private final String memberId;

    public MemberIdRequestWrapper(HttpServletRequest request, Long memberId) {
        super(request);
        this.memberId = memberId == null ? null : String.valueOf(memberId);
    }

    @Override
    public String getHeader(String name) {
        if (HEADER.equalsIgnoreCase(name)) {
            return memberId;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (HEADER.equalsIgnoreCase(name)) {
            return Collections.enumeration(memberId == null ? List.of() : List.of(memberId));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = new ArrayList<>();
        for (String n : Collections.list(super.getHeaderNames())) {
            if (!HEADER.equalsIgnoreCase(n)) {
                names.add(n);
            }
        }
        if (memberId != null) {
            names.add(HEADER);
        }
        return Collections.enumeration(names);
    }
}
