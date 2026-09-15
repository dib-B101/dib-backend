package com.b101.dib.address.query.dto;

import java.util.List;

public record AddressListResponse(
        /** 로그인한 회원이 등록한 배송지 목록 */
        List<AddressQueryDto> items
) {
}
