package com.b101.dib.payment.toss;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TossApiException extends BusinessException {
    private final String tossMessage;

    public TossApiException(ErrorCode errorCode, String tossMessage) {
        super(errorCode);
        this.tossMessage = tossMessage;
    }
}
