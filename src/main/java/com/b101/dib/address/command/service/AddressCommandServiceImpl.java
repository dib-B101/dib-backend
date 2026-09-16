package com.b101.dib.address.command.service;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.command.dto.UpdateAddressRequest;
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

    @Override
    public AddressResponse update(Long memberId, Long addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        address.update(
                request.number() == null ? address.getNumber() : trimToNull(request.number()),
                request.address() == null ? address.getAddress() : trimToNull(request.address()),
                request.name() == null ? address.getName() : request.name().trim(),
                request.apiAddressId() == null
                        ? address.getApiAddressId()
                        : request.apiAddressId().trim()
        );

        return toResponse(address);
    }

    @Override
    public void delete(Long memberId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        addressRepository.delete(address);
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
