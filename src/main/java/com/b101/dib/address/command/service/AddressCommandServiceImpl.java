package com.b101.dib.address.command.service;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.domain.Address;
import com.b101.dib.address.repository.AddressRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressCommandServiceImpl implements AddressCommandService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Override
    public AddressResponse create(Long memberId, CreateAddressRequest request) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Address address = Address.builder()
                .memberId(memberId)
                .number(trimToNull(request.number()))
                .address(trimToNull(request.address()))
                .name(request.name().trim())
                .apiAddressId(request.apiAddressId().trim())
                .build();

        return toResponse(addressRepository.save(address));
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getNumber(),
                address.getAddress(),
                address.getName(),
                address.getApiAddressId()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
