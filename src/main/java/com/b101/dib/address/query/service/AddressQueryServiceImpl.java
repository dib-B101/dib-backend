package com.b101.dib.address.query.service;

import java.util.List;

import com.b101.dib.address.query.dto.AddressQueryDto;
import com.b101.dib.address.repository.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressQueryServiceImpl implements AddressQueryService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressQueryDto> findMine(Long memberId) {
        return addressMapper.findByMemberId(memberId);
    }
}
