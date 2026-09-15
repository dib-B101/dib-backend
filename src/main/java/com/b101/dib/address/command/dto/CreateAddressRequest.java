package com.b101.dib.address.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        /** 우편번호 */
        @Size(max = 50) String number,

        /** 도로명 또는 지번 주소 */
        @Size(max = 500) String address,

        /** 사용자가 지정한 배송지 이름 */
        @NotBlank @Size(max = 100) String name,

        /** 외부 주소 검색 API의 주소 식별자 */
        @NotBlank @Size(max = 500) String apiAddressId
) {
}
