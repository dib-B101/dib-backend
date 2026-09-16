package com.b101.dib.bid.query.service;

import java.time.LocalDateTime;
import java.util.List;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.bid.repository.BidMapper;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BidQueryServiceImplTest {

    @Mock
    private BidMapper bidMapper;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private BidQueryServiceImpl bidQueryService;

    @Test
    void returnsMemberBidHistoryWithCursorPagination() {
        given(bidMapper.findByMemberId(1L, AuctionStatus.ACTIVE, 30L, 3))
                .willReturn(List.of(bid(29L), bid(28L), bid(27L)));

        CursorPageDto<MyBidQueryDto> result = bidQueryService.findMine(
                1L,
                AuctionStatus.ACTIVE,
                "30",
                2
        );

        assertThat(result.getItems()).extracting(MyBidQueryDto::getBidId)
                .containsExactly(29L, 28L);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("28");
        verify(bidMapper).findByMemberId(1L, AuctionStatus.ACTIVE, 30L, 3);
    }

    @Test
    void rejectsInvalidCursor() {
        assertThatThrownBy(() -> bidQueryService.findMine(1L, null, "invalid", 20))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CURSOR)
                );
    }

    private MyBidQueryDto bid(Long bidId) {
        MyBidQueryDto dto = new MyBidQueryDto();
        dto.setBidId(bidId);
        dto.setAuctionId(10L);
        dto.setAmount(100_000L);
        dto.setCreatedAt(LocalDateTime.of(2026, 9, 16, 3, 0));
        return dto;
    }
}
