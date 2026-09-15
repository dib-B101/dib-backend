package com.b101.dib.auth.command.event;

import java.time.Instant;

/** 비밀번호 변경 이벤트 */
public record PasswordChangedEvent(
        String eventId,
        Long memberId,
        Instant changedAt
) {
}
