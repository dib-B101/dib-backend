package com.b101.dib.address.command.service;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;

public interface AddressCommandService {

    AddressResponse create(Long memberId, CreateAddressRequest request);
}
