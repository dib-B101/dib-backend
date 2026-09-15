package com.b101.dib.address.command.service;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.domain.Address;
import com.b101.dib.address.repository.AddressRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressCommandServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AddressCommandServiceImpl addressCommandService;

    @Test
    void createsAddressOwnedByAuthenticatedMember() {
        given(memberRepository.existsById(1L)).willReturn(true);
        given(addressRepository.save(any(Address.class))).willAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            return Address.builder()
                    .id(10L)
                    .memberId(address.getMemberId())
                    .number(address.getNumber())
                    .address(address.getAddress())
                    .name(address.getName())
                    .apiAddressId(address.getApiAddressId())
                    .build();
        });

        AddressResponse result = addressCommandService.create(
                1L,
                new CreateAddressRequest(
                        " 06236 ",
                        " 서울특별시 강남구 테헤란로 ",
                        " 회사 ",
                        " address-api-10 "
                )
        );

        assertThat(result).isEqualTo(new AddressResponse(
                10L,
                "06236",
                "서울특별시 강남구 테헤란로",
                "회사",
                "address-api-10"
        ));
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void storesBlankOptionalFieldsAsNull() {
        given(memberRepository.existsById(1L)).willReturn(true);
        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AddressResponse result = addressCommandService.create(
                1L,
                new CreateAddressRequest(" ", null, "집", "address-api-10")
        );

        assertThat(result.number()).isNull();
        assertThat(result.address()).isNull();
    }

    @Test
    void rejectsWhenAuthenticatedMemberNoLongerExists() {
        given(memberRepository.existsById(1L)).willReturn(false);

        assertThatThrownBy(() -> addressCommandService.create(
                1L,
                new CreateAddressRequest("06236", "서울특별시 강남구", "집", "address-api-10")
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
        );

        verify(addressRepository, never()).save(any(Address.class));
    }
}
