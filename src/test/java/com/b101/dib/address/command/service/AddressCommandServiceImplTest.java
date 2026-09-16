package com.b101.dib.address.command.service;

import java.util.Optional;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.command.dto.UpdateAddressRequest;
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

    @Test
    void updatesOnlyProvidedAddressFields() {
        Address address = address(10L, 1L);
        given(addressRepository.findById(10L)).willReturn(Optional.of(address));

        AddressResponse result = addressCommandService.update(
                1L,
                10L,
                new UpdateAddressRequest(null, null, " 새 회사 ", " new-address-api-id ")
        );

        assertThat(result).isEqualTo(new AddressResponse(
                10L,
                "06236",
                "서울특별시 강남구 테헤란로",
                "새 회사",
                "new-address-api-id"
        ));
    }

    @Test
    void clearsOptionalAddressFieldsWhenBlankValuesAreProvided() {
        Address address = address(10L, 1L);
        given(addressRepository.findById(10L)).willReturn(Optional.of(address));

        AddressResponse result = addressCommandService.update(
                1L,
                10L,
                new UpdateAddressRequest(" ", "", null, null)
        );

        assertThat(result.number()).isNull();
        assertThat(result.address()).isNull();
        assertThat(result.name()).isEqualTo("회사");
        assertThat(result.apiAddressId()).isEqualTo("address-api-10");
    }

    @Test
    void rejectsUpdateWhenAddressDoesNotExist() {
        given(addressRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressCommandService.update(
                1L,
                10L,
                new UpdateAddressRequest(null, null, "새 회사", null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND)
        );
    }

    @Test
    void rejectsUpdateWhenAddressBelongsToAnotherMember() {
        given(addressRepository.findById(10L)).willReturn(Optional.of(address(10L, 2L)));

        assertThatThrownBy(() -> addressCommandService.update(
                1L,
                10L,
                new UpdateAddressRequest(null, null, "새 회사", null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN)
        );
    }

    @Test
    void deletesAddressOwnedByAuthenticatedMember() {
        Address address = address(10L, 1L);
        given(addressRepository.findById(10L)).willReturn(Optional.of(address));

        addressCommandService.delete(1L, 10L);

        verify(addressRepository).delete(address);
    }

    @Test
    void rejectsDeleteWhenAddressDoesNotExist() {
        given(addressRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressCommandService.delete(1L, 10L))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ADDRESS_NOT_FOUND)
                );

        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void rejectsDeleteWhenAddressBelongsToAnotherMember() {
        given(addressRepository.findById(10L)).willReturn(Optional.of(address(10L, 2L)));

        assertThatThrownBy(() -> addressCommandService.delete(1L, 10L))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.FORBIDDEN)
                );

        verify(addressRepository, never()).delete(any(Address.class));
    }

    private Address address(Long addressId, Long memberId) {
        return Address.builder()
                .id(addressId)
                .memberId(memberId)
                .number("06236")
                .address("서울특별시 강남구 테헤란로")
                .name("회사")
                .apiAddressId("address-api-10")
                .build();
    }
}
