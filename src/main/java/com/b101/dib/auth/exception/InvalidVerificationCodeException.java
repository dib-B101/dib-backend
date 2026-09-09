package com.b101.dib.auth.exception;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidVerificationCodeException extends BusinessException {

    private final int remainingAttempts;

    public InvalidVerificationCodeException(int remainingAttempts) {
        super(ErrorCode.INVALID_CODE);
        this.remainingAttempts = remainingAttempts;
    }
}
