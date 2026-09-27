package com.b101.dib.address.command.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        /** 우편번호 */
        @Size(max = 50) String number,

        /** 도로명 또는 지번 주소 */
        @Size(max = 500) String address,

        /** 사용자가 지정한 배송지 이름 */
        @Size(max = 100) String name,

        /** 외부 주소 검색 API의 주소 식별자 */
        @Size(max = 500) String apiAddressId,

        @Size(max = 500) String detailAddress,
        @Size(max = 100) String receiverName,
        @Size(max = 20) @Pattern(regexp = "[0-9 -]{10,20}") String receiverPhone
) {
    public UpdateAddressRequest(String number, String address, String name, String apiAddressId) {
        this(number, address, name, apiAddressId, null, null, null);
    }

    @AssertTrue
    public boolean isValidUpdate() {
        return hasUpdateField()
                && (name == null || !name.isBlank())
                && (apiAddressId == null || !apiAddressId.isBlank())
                && (receiverName == null || !receiverName.isBlank())
                && (receiverPhone == null || receiverPhone.replaceAll("[^0-9]", "").matches("[0-9]{10,11}"));
    }

    private boolean hasUpdateField() {
        return number != null || address != null || name != null || apiAddressId != null
                || detailAddress != null || receiverName != null || receiverPhone != null;
    }
}
