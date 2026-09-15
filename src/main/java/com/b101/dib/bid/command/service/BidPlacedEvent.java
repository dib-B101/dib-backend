package com.b101.dib.bid.command.service;

import com.b101.dib.bid.command.dto.BidPlacedDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 입찰 트랜잭션 커밋 후 소켓 브로드캐스트용 (websocket 패키지가 구독)
@Getter
@AllArgsConstructor
public class BidPlacedEvent {
    private final BidPlacedDto placed;
}
