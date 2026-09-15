package com.b101.dib.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

// DB/엔티티는 LocalDateTime, 소켓·스냅샷처럼 프론트가 Instant.parse 하는 곳은 ISO UTC 문자열로
public final class Times {
    private Times() {}

    public static String iso(LocalDateTime time) {
        return time == null ? null : time.atZone(ZoneId.systemDefault()).toInstant().toString();
    }

    public static String now() {
        return iso(LocalDateTime.now());
    }
}
