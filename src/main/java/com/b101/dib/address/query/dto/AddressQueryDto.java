package com.b101.dib.address.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressQueryDto {

    /** 배송지 식별자 */
    private Long addressId;

    /** 우편번호 */
    private String number;

    /** 도로명 또는 지번 주소 */
    private String address;

    /** 사용자가 지정한 배송지 이름 */
    private String name;

    /** 외부 주소 검색 API의 주소 식별자 */
    private String apiAddressId;
}
