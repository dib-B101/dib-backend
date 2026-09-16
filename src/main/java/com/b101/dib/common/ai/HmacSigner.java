package com.b101.dib.common.ai;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

// AI 서버와의 HMAC 규약: 서명 대상 "{timestamp}.{본문 원문}", HMAC-SHA256 16진수, 헤더 X-DIB-Timestamp / X-DIB-Signature: sha256=<hex>
public final class HmacSigner {
    public static final String TIMESTAMP_HEADER = "X-DIB-Timestamp";
    public static final String SIGNATURE_HEADER = "X-DIB-Signature";

    private HmacSigner() {}

    public static String sign(String secret, long timestamp, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            mac.update(body);
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal());
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 서명 실패", e);
        }
    }

    public static long nowSeconds() {
        return Instant.now().getEpochSecond();
    }

    // 콜백 검증. 시각 오차·서명 불일치면 false
    public static boolean verify(String secret, String timestampHeader, String signatureHeader, byte[] body, int maxSkewSeconds) {
        if (secret == null || secret.isBlank() || timestampHeader == null || signatureHeader == null) {
            return false;
        }
        long ts;
        try {
            ts = Long.parseLong(timestampHeader.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(nowSeconds() - ts) > maxSkewSeconds) {
            return false;
        }
        String expected = sign(secret, ts, body);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signatureHeader.trim().getBytes(StandardCharsets.UTF_8));
    }
}
