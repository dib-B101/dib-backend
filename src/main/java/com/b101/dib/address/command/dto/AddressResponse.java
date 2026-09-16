package com.b101.dib.address.command.dto;

public record AddressResponse(
        /** 배송지 식별자 */
        Long addressId,

        /** 우편번호 */
        String number,

        /** 도로명 또는 지번 주소 */
        String address,

        /** 사용자가 지정한 배송지 이름 */
        String name,

        /** 외부 주소 검색 API의 주소 식별자 */
        String apiAddressId
) {
}
