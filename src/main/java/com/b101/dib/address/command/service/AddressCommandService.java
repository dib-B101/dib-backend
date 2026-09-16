package com.b101.dib.address.command.service;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.command.dto.UpdateAddressRequest;

public interface AddressCommandService {

    AddressResponse create(Long memberId, CreateAddressRequest request);

    AddressResponse update(Long memberId, Long addressId, UpdateAddressRequest request);

    void delete(Long memberId, Long addressId);
}
