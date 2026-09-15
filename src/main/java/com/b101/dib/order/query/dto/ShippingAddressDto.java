package com.b101.dib.order.query.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShippingAddressDto {
    // order.address 에 JSON 문자열로 저장돼 있어 그대로 객체로 내보낸다 → data: { address: { zip, address, detail, receiverName, receiverPhone } }
    @JsonRawValue
    private String address;
}
