package com.b101.dib.settlement.repository;

import com.b101.dib.settlement.query.dto.SettlementDetailDto;
import com.b101.dib.settlement.query.dto.SettlementQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SettlementMapper {
    List<SettlementQueryDto> findBySellerId(@Param("sellerId") Long sellerId,
                                            @Param("cursor") Long cursor,
                                            @Param("limit") int limit);
    SettlementDetailDto findById(@Param("settlementId") Long settlementId);
}
