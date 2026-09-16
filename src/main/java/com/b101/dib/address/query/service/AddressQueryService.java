package com.b101.dib.address.query.service;

import java.util.List;

import com.b101.dib.address.query.dto.AddressQueryDto;

public interface AddressQueryService {

    List<AddressQueryDto> findMine(Long memberId);
}
