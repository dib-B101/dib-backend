package com.b101.dib.order.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum Carrier {
    CJ("CJ대한통운", "kr.cjlogistics", "04", false),
    HANJIN("한진택배", "kr.hanjin", "05", false),
    LOGEN("로젠택배", "kr.logen", "06", false),
    LOTTE("롯데택배", "kr.lotte", "08", false),
    EPOST("우체국택배", "kr.epost", "01", false),
    DUMMY("테스트 택배사", "dev.track.dummy", null, true);

    private final String displayName;
    private final String deliveryTrackerId;
    private final String sweetTrackerCode;
    private final boolean testOnly;

    Carrier(String displayName, String deliveryTrackerId, String sweetTrackerCode, boolean testOnly) {
        this.displayName = displayName;
        this.deliveryTrackerId = deliveryTrackerId;
        this.sweetTrackerCode = sweetTrackerCode;
        this.testOnly = testOnly;
    }

    public static Carrier from(String code) {
        if (code == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_CARRIER);
        }
        try {
            return Carrier.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_CARRIER);
        }
    }
}
