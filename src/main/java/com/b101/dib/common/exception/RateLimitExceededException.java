package com.b101.dib.common.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends BusinessException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super(ErrorCode.RATE_LIMITED);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
