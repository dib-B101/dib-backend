package com.b101.dib.settlement.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.AccountMasker;
import com.b101.dib.settlement.query.dto.SettlementDetailDto;
import com.b101.dib.settlement.query.dto.SettlementQueryDto;
import com.b101.dib.settlement.repository.SettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementQueryServiceImpl implements SettlementQueryService {
    private final SettlementMapper settlementMapper;

    @Override
    public CursorPageDto<SettlementQueryDto> findMine(Long sellerId, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<SettlementQueryDto> rows = settlementMapper.findBySellerId(sellerId, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, SettlementQueryDto::getSettlementId);
    }

    @Override
    public SettlementDetailDto findDetail(Long memberId, Long settlementId) {
        SettlementDetailDto dto = settlementMapper.findById(settlementId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }
        if (!dto.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        dto.setMaskedAccountNumber(AccountMasker.mask(dto.getMaskedAccountNumber()));
        return dto;
    }
}
