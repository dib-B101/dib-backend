package com.b101.dib.order.carrier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarrierTrackingCache {
    private static final String FS = "\u001F";
    private static final String RS = "\u001E";
    private static final String NULL = "\u0000";

    private final StringRedisTemplate redisTemplate;

    @Value("${carrier.cache-seconds:300}")
    private long cacheSeconds;

    public Optional<CarrierTracking> get(String carrier, String trackingNumber) {
        if (cacheSeconds <= 0) return Optional.empty();
        try {
            String raw = redisTemplate.opsForValue().get(key(carrier, trackingNumber));
            return raw == null ? Optional.empty() : Optional.of(decode(raw));
        } catch (Exception e) {
            log.warn("배송 조회 캐시 읽기 실패 (무시) carrier={} tracking={}", carrier, trackingNumber, e);
            return Optional.empty();
        }
    }

    public void put(String carrier, String trackingNumber, CarrierTracking tracking) {
        if (cacheSeconds <= 0) return;
        try {
            redisTemplate.opsForValue().set(key(carrier, trackingNumber), encode(tracking), Duration.ofSeconds(cacheSeconds));
        } catch (Exception e) {
            log.warn("배송 조회 캐시 쓰기 실패 (무시) carrier={} tracking={}", carrier, trackingNumber, e);
        }
    }

    private static String key(String carrier, String trackingNumber) {
        return "carrier:track:" + carrier + ":" + trackingNumber;
    }

    static String encode(CarrierTracking t) {
        StringBuilder sb = new StringBuilder();
        sb.append(n(t.status())).append(FS).append(t.delivered());
        for (CarrierEvent e : t.events()) {
            sb.append(RS).append(n(e.time())).append(FS).append(n(e.status()))
              .append(FS).append(n(e.description())).append(FS).append(n(e.location()));
        }
        return sb.toString();
    }

    static CarrierTracking decode(String raw) {
        String[] rows = raw.split(RS, -1);
        String[] head = rows[0].split(FS, -1);
        List<CarrierEvent> events = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            String[] f = rows[i].split(FS, -1);
            if (f.length < 4) continue;
            events.add(new CarrierEvent(d(f[0]), d(f[1]), d(f[2]), d(f[3])));
        }
        return new CarrierTracking(d(head[0]), Boolean.parseBoolean(head[1]), events);
    }

    private static String n(String s) { return s == null ? NULL : s; }
    private static String d(String s) { return NULL.equals(s) ? null : s; }
}
