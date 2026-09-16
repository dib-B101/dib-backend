package com.b101.dib.auction.query.service;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.common.dto.CursorPageDto;

public interface MemberSaleQueryService {
    /** 특정 회원의 판매 경매 내역을 조회한다. */
    CursorPageDto<SaleHistoryQueryDto> findMine(
            Long memberId,
            AuctionStatus auctionStatus,
            String cursor,
            int size
    );
}
