package com.b101.dib.auction.command.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 경매의 조건·상태가 커맨드로 바뀌었다(시작·조건 수정·재등록·취소).
 * 입찰 스냅샷 Hot State(auction:{id}:snapshot)는 입찰·종료 때만 갱신됐기 때문에,
 * DB 를 다시 시드해 같은 auctionId 가 재사용되면 Redis 에 남은 예전 경매 값(현재가·마감)이
 * 새 경매의 상세에 그대로 보였다. 이 이벤트를 받는 쪽이 커밋 뒤 DB 기준으로 스냅샷을 덮어쓴다.
 */
@Getter
@AllArgsConstructor
public class AuctionStateChangedEvent {
    private final Long auctionId;
}
