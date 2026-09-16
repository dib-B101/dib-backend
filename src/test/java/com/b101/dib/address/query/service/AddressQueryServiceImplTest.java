package com.b101.dib.address.query.service;

import java.util.List;

import com.b101.dib.address.query.dto.AddressQueryDto;
import com.b101.dib.address.repository.AddressMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressQueryServiceImplTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressQueryServiceImpl addressQueryService;

    @Test
    void returnsAddressesOwnedByMember() {
        AddressQueryDto address = new AddressQueryDto();
        given(addressMapper.findByMemberId(1L)).willReturn(List.of(address));

        List<AddressQueryDto> result = addressQueryService.findMine(1L);

        assertThat(result).containsExactly(address);
        verify(addressMapper).findByMemberId(1L);
    }

    @Test
    void returnsEmptyListWhenMemberHasNoAddress() {
        given(addressMapper.findByMemberId(1L)).willReturn(List.of());

        List<AddressQueryDto> result = addressQueryService.findMine(1L);

        assertThat(result).isEmpty();
    }
}
