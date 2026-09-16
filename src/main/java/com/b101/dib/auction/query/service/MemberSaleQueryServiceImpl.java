package com.b101.dib.auction.query.service;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.auction.query.dto.SaleHistoryRowDto;
import com.b101.dib.auction.repository.AuctionMapper;
import com.b101.dib.common.dto.CursorPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSaleQueryServiceImpl implements MemberSaleQueryService {
    private final AuctionMapper auctionMapper;

    @Override
    public CursorPageDto<SaleHistoryQueryDto> findMine(
            Long memberId,
            AuctionStatus auctionStatus,
            String cursor,
            int size
    ) {
        int limit = CursorPageDto.limit(size);
        List<SaleHistoryQueryDto> rows = auctionMapper.findSalesByMemberId(
                        memberId,
                        auctionStatus,
                        CursorPageDto.parseCursor(cursor),
                        limit + 1
                ).stream()
                .map(SaleHistoryQueryDto::from)
                .toList();

        return CursorPageDto.of(
                rows,
                limit,
                row -> row.getAuction().getAuctionId()
        );
    }
}
